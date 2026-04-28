package de.sofia.sofias_ordainment.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.sofia.sofias_ordainment.Sofias_ordainment;
import de.sofia.sofias_ordainment.entity.CockroachSwarmMarked;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BatEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SwarmModelRenderer extends BatEntityRenderer {
    private static final Identifier TEXTURE = new Identifier(
            Sofias_ordainment.MOD_ID,
            "textures/entity/cockroach_boxuv_fixed.png"
    );
    private static final Identifier MODEL = new Identifier(
            Sofias_ordainment.MOD_ID,
            "models/entity/cockroach_swarm.bbmodel"
    );

    private CockroachSwarmModel cockroachModel;
    private boolean modelLoadFailed;
    private final Map<UUID, Float> lastMovementYaws = new ConcurrentHashMap<>();

    public SwarmModelRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            BatEntity bat,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light
    ) {
        if (!(bat instanceof CockroachSwarmMarked marker)
                || !marker.sofias_ordainment$isCockroachSwarm()) {
            super.render(bat, yaw, tickDelta, matrices, vertexConsumers, light);
            return;
        }

        CockroachSwarmModel model = getCockroachModel();
        if (model == null) {
            super.render(bat, yaw, tickDelta, matrices, vertexConsumers, light);
            return;
        }

        matrices.push();
        boolean hasTarget = marker.sofias_ordainment$hasCockroachSwarmTarget();
        matrices.translate(0.0D, 0.35D, 0.0D);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - getStableMovementYaw(bat, yaw)));
        if (!hasTarget) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
        }

        double ticks = getRenderTicks(bat, tickDelta);
        float hover = (float) Math.sin(ticks * 0.3D + bat.getId()) * 0.03F;
        matrices.translate(0.0D, hover, 0.0D);

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
        model.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV, (ticks + getAnimationPhaseTicks(bat)) / 20.0D, hasTarget);
        matrices.pop();
    }

    private float getStableMovementYaw(BatEntity bat, float fallbackYaw) {
        Vec3d velocity = bat.getVelocity();
        double horizontalSpeedSquared = velocity.x * velocity.x + velocity.z * velocity.z;
        if (horizontalSpeedSquared > 1.0E-5D) {
            float movementYaw = (float) (Math.toDegrees(Math.atan2(velocity.z, velocity.x)) - 90.0D);
            lastMovementYaws.put(bat.getUuid(), movementYaw);
            cleanupYawCache();
            return movementYaw;
        }

        return lastMovementYaws.getOrDefault(bat.getUuid(), fallbackYaw);
    }

    private double getRenderTicks(BatEntity bat, float tickDelta) {
        if (bat.getWorld() != null) {
            return bat.getWorld().getTime() + tickDelta;
        }

        return bat.age + tickDelta;
    }

    private double getAnimationPhaseTicks(BatEntity bat) {
        return Math.floorMod(bat.getUuid().hashCode(), 40);
    }

    private void cleanupYawCache() {
        if (lastMovementYaws.size() < 128) return;

        lastMovementYaws.clear();
    }

    private CockroachSwarmModel getCockroachModel() {
        if (cockroachModel != null || modelLoadFailed) {
            return cockroachModel;
        }

        try {
            Resource resource = MinecraftClient.getInstance()
                    .getResourceManager()
                    .getResource(MODEL)
                    .orElseThrow();
            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                cockroachModel = CockroachSwarmModel.load(JsonParser.parseReader(reader).getAsJsonObject());
            }
        } catch (Exception e) {
            modelLoadFailed = true;
            Sofias_ordainment.LOGGER.error("Failed to load cockroach swarm model", e);
        }

        return cockroachModel;
    }

    private static class CockroachSwarmModel {
        private static final double PIXEL = 1.0D / 16.0D;

        private final ModelNode root;
        private final Animation flightAnimation;
        private final Animation idleAnimation;
        private final int textureWidth;
        private final int textureHeight;

        private CockroachSwarmModel(ModelNode root, Animation flightAnimation, Animation idleAnimation, int textureWidth, int textureHeight) {
            this.root = root;
            this.flightAnimation = flightAnimation;
            this.idleAnimation = idleAnimation;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
        }

        private static CockroachSwarmModel load(JsonObject json) {
            Map<String, ModelCube> cubes = new HashMap<>();
            for (JsonElement element : json.getAsJsonArray("elements")) {
                ModelCube cube = ModelCube.fromJson(element.getAsJsonObject());
                cubes.put(cube.uuid, cube);
            }

            JsonObject resolution = json.getAsJsonObject("resolution");
            int textureWidth = resolution.get("width").getAsInt();
            int textureHeight = resolution.get("height").getAsInt();
            JsonObject rootJson = json.getAsJsonArray("outliner").get(0).getAsJsonObject();
            Animation flightAnimation = Animation.fromJson(json, "sealfly");
            Animation idleAnimation = Animation.fromJson(json, "cockroach.dance_spin_base_pose_safe");

            return new CockroachSwarmModel(
                    ModelNode.fromJson(rootJson, cubes),
                    flightAnimation,
                    idleAnimation,
                    textureWidth,
                    textureHeight
            );
        }

        private void render(MatrixStack matrices, VertexConsumer buffer, int light, int overlay, double seconds, boolean hasTarget) {
            Animation animation = hasTarget ? flightAnimation : idleAnimation;
            double animationTime = animation.length <= 0.0D ? 0.0D : seconds % animation.length;
            root.render(matrices, buffer, light, overlay, textureWidth, textureHeight, animation, animationTime);
        }
    }

    private static class ModelNode {
        private final String uuid;
        private final Vec3 origin;
        private final Vec3 rotation;
        private final List<ModelNode> children;
        private final List<ModelCube> cubes;

        private ModelNode(String uuid, Vec3 origin, Vec3 rotation, List<ModelNode> children, List<ModelCube> cubes) {
            this.uuid = uuid;
            this.origin = origin;
            this.rotation = rotation;
            this.children = children;
            this.cubes = cubes;
        }

        private static ModelNode fromJson(JsonObject json, Map<String, ModelCube> availableCubes) {
            List<ModelNode> children = new ArrayList<>();
            List<ModelCube> cubes = new ArrayList<>();
            JsonElement childrenElement = json.get("children");

            if (childrenElement instanceof JsonArray childrenArray) {
                for (JsonElement child : childrenArray) {
                    if (child.isJsonObject()) {
                        children.add(fromJson(child.getAsJsonObject(), availableCubes));
                    } else {
                        addCubeRefs(child.getAsString(), availableCubes, cubes);
                    }
                }
            } else if (childrenElement != null && childrenElement.isJsonPrimitive()) {
                addCubeRefs(childrenElement.getAsString(), availableCubes, cubes);
            }

            return new ModelNode(
                    json.get("uuid").getAsString(),
                    Vec3.fromJson(json.get("origin")),
                    Vec3.fromJson(json.get("rotation")),
                    children,
                    cubes
            );
        }

        private static void addCubeRefs(String cubeRefs, Map<String, ModelCube> availableCubes, List<ModelCube> cubes) {
            for (String ref : cubeRefs.split("\\s+")) {
                if (ref.isBlank()) continue;

                ModelCube cube = availableCubes.get(ref);
                if (cube != null) {
                    cubes.add(cube);
                }
            }
        }

        private void render(
                MatrixStack matrices,
                VertexConsumer buffer,
                int light,
                int overlay,
                int textureWidth,
                int textureHeight,
                Animation animation,
                double animationTime
        ) {
            Transform transform = animation.getTransform(uuid, animationTime);

            matrices.push();
            matrices.translate(
                    transform.position.x * CockroachSwarmModel.PIXEL,
                    transform.position.y * CockroachSwarmModel.PIXEL,
                    transform.position.z * CockroachSwarmModel.PIXEL
            );
            matrices.translate(
                    origin.x * CockroachSwarmModel.PIXEL,
                    origin.y * CockroachSwarmModel.PIXEL,
                    origin.z * CockroachSwarmModel.PIXEL
            );

            Vec3 animatedRotation = rotation.add(transform.rotation);
            rotate(matrices, animatedRotation);

            matrices.translate(
                    -origin.x * CockroachSwarmModel.PIXEL,
                    -origin.y * CockroachSwarmModel.PIXEL,
                    -origin.z * CockroachSwarmModel.PIXEL
            );

            for (ModelCube cube : cubes) {
                cube.render(matrices, buffer, light, overlay, textureWidth, textureHeight);
            }
            for (ModelNode child : children) {
                child.render(matrices, buffer, light, overlay, textureWidth, textureHeight, animation, animationTime);
            }

            matrices.pop();
        }

        private static void rotate(MatrixStack matrices, Vec3 rotation) {
            if (rotation.z != 0.0D) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) rotation.z));
            }
            if (rotation.y != 0.0D) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) rotation.y));
            }
            if (rotation.x != 0.0D) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) rotation.x));
            }
        }
    }

    private static class ModelCube {
        private final String uuid;
        private final Vec3 from;
        private final Vec3 to;
        private final Map<String, Face> faces;

        private ModelCube(String uuid, Vec3 from, Vec3 to, Map<String, Face> faces) {
            this.uuid = uuid;
            this.from = from;
            this.to = to;
            this.faces = faces;
        }

        private static ModelCube fromJson(JsonObject json) {
            Map<String, Face> faces = new HashMap<>();
            JsonObject facesJson = json.getAsJsonObject("faces");
            for (String faceName : List.of("north", "east", "south", "west", "up", "down")) {
                JsonObject faceJson = facesJson.getAsJsonObject(faceName);
                if (faceJson != null && faceJson.has("uv")) {
                    faces.put(faceName, Face.fromJson(faceJson.getAsJsonArray("uv")));
                }
            }

            return new ModelCube(
                    json.get("uuid").getAsString(),
                    Vec3.fromJson(json.get("from")),
                    Vec3.fromJson(json.get("to")),
                    faces
            );
        }

        private void render(
                MatrixStack matrices,
                VertexConsumer buffer,
                int light,
                int overlay,
                int textureWidth,
                int textureHeight
        ) {
            MatrixStack.Entry entry = matrices.peek();
            Matrix4f positionMatrix = entry.getPositionMatrix();
            Matrix3f normalMatrix = entry.getNormalMatrix();

            float x1 = (float) (from.x * CockroachSwarmModel.PIXEL);
            float y1 = (float) (from.y * CockroachSwarmModel.PIXEL);
            float z1 = (float) (from.z * CockroachSwarmModel.PIXEL);
            float x2 = (float) (to.x * CockroachSwarmModel.PIXEL);
            float y2 = (float) (to.y * CockroachSwarmModel.PIXEL);
            float z2 = (float) (to.z * CockroachSwarmModel.PIXEL);

            Face north = faces.get("north");
            if (north != null) {
                quad(buffer, positionMatrix, normalMatrix, light, overlay, textureWidth, textureHeight, north,
                        x1, y2, z1, x2, y2, z1, x2, y1, z1, x1, y1, z1, 0.0F, 0.0F, -1.0F);
            }
            Face south = faces.get("south");
            if (south != null) {
                quad(buffer, positionMatrix, normalMatrix, light, overlay, textureWidth, textureHeight, south,
                        x2, y2, z2, x1, y2, z2, x1, y1, z2, x2, y1, z2, 0.0F, 0.0F, 1.0F);
            }
            Face east = faces.get("east");
            if (east != null) {
                quad(buffer, positionMatrix, normalMatrix, light, overlay, textureWidth, textureHeight, east,
                        x2, y2, z1, x2, y2, z2, x2, y1, z2, x2, y1, z1, 1.0F, 0.0F, 0.0F);
            }
            Face west = faces.get("west");
            if (west != null) {
                quad(buffer, positionMatrix, normalMatrix, light, overlay, textureWidth, textureHeight, west,
                        x1, y2, z2, x1, y2, z1, x1, y1, z1, x1, y1, z2, -1.0F, 0.0F, 0.0F);
            }
            Face up = faces.get("up");
            if (up != null) {
                quad(buffer, positionMatrix, normalMatrix, light, overlay, textureWidth, textureHeight, up,
                        x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, 0.0F, 1.0F, 0.0F);
            }
            Face down = faces.get("down");
            if (down != null) {
                quad(buffer, positionMatrix, normalMatrix, light, overlay, textureWidth, textureHeight, down,
                        x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, 0.0F, -1.0F, 0.0F);
            }
        }

        private static void quad(
                VertexConsumer buffer,
                Matrix4f positionMatrix,
                Matrix3f normalMatrix,
                int light,
                int overlay,
                int textureWidth,
                int textureHeight,
                Face face,
                float x1,
                float y1,
                float z1,
                float x2,
                float y2,
                float z2,
                float x3,
                float y3,
                float z3,
                float x4,
                float y4,
                float z4,
                float nx,
                float ny,
                float nz
        ) {
            float u1 = face.u1 / textureWidth;
            float v1 = face.v1 / textureHeight;
            float u2 = face.u2 / textureWidth;
            float v2 = face.v2 / textureHeight;

            vertex(buffer, positionMatrix, normalMatrix, x1, y1, z1, u1, v1, light, overlay, nx, ny, nz);
            vertex(buffer, positionMatrix, normalMatrix, x2, y2, z2, u2, v1, light, overlay, nx, ny, nz);
            vertex(buffer, positionMatrix, normalMatrix, x3, y3, z3, u2, v2, light, overlay, nx, ny, nz);
            vertex(buffer, positionMatrix, normalMatrix, x4, y4, z4, u1, v2, light, overlay, nx, ny, nz);
        }

        private static void vertex(
                VertexConsumer buffer,
                Matrix4f positionMatrix,
                Matrix3f normalMatrix,
                float x,
                float y,
                float z,
                float u,
                float v,
                int light,
                int overlay,
                float nx,
                float ny,
                float nz
        ) {
            buffer.vertex(positionMatrix, x, y, z)
                    .color(255, 255, 255, 255)
                    .texture(u, v)
                    .overlay(overlay)
                    .light(light)
                    .normal(normalMatrix, nx, ny, nz)
                    .next();
        }
    }

    private static class Face {
        private final float u1;
        private final float v1;
        private final float u2;
        private final float v2;

        private Face(float u1, float v1, float u2, float v2) {
            this.u1 = u1;
            this.v1 = v1;
            this.u2 = u2;
            this.v2 = v2;
        }

        private static Face fromJson(JsonArray json) {
            return new Face(
                    json.get(0).getAsFloat(),
                    json.get(1).getAsFloat(),
                    json.get(2).getAsFloat(),
                    json.get(3).getAsFloat()
            );
        }
    }

    private static class Animation {
        private final double length;
        private final Map<String, List<Keyframe>> rotations;
        private final Map<String, List<Keyframe>> positions;

        private Animation(double length, Map<String, List<Keyframe>> rotations, Map<String, List<Keyframe>> positions) {
            this.length = length;
            this.rotations = rotations;
            this.positions = positions;
        }

        private static Animation fromJson(JsonObject modelJson, String name) {
            JsonArray animations = modelJson.getAsJsonArray("animations");
            for (JsonElement element : animations) {
                JsonObject animationJson = element.getAsJsonObject();
                if (!name.equals(animationJson.get("name").getAsString())) continue;

                Map<String, List<Keyframe>> rotations = new HashMap<>();
                Map<String, List<Keyframe>> positions = new HashMap<>();
                JsonObject animators = animationJson.getAsJsonObject("animators");
                for (Map.Entry<String, JsonElement> animatorEntry : animators.entrySet()) {
                    String uuid = animatorEntry.getKey();
                    for (JsonElement keyframeElement : animatorEntry.getValue().getAsJsonObject().getAsJsonArray("keyframes")) {
                        JsonObject keyframeJson = keyframeElement.getAsJsonObject();
                        String channel = keyframeJson.get("channel").getAsString();
                        Keyframe keyframe = Keyframe.fromJson(keyframeJson);
                        if ("rotation".equals(channel)) {
                            rotations.computeIfAbsent(uuid, key -> new ArrayList<>()).add(keyframe);
                        } else if ("position".equals(channel)) {
                            positions.computeIfAbsent(uuid, key -> new ArrayList<>()).add(keyframe);
                        }
                    }
                }

                sortKeyframes(rotations);
                sortKeyframes(positions);
                return new Animation(animationJson.get("length").getAsDouble(), rotations, positions);
            }

            return new Animation(0.0D, Map.of(), Map.of());
        }

        private static void sortKeyframes(Map<String, List<Keyframe>> keyframes) {
            for (List<Keyframe> frames : keyframes.values()) {
                frames.sort(Comparator.comparingDouble(frame -> frame.time));
            }
        }

        private Transform getTransform(String uuid, double time) {
            return new Transform(
                    sample(positions.get(uuid), time),
                    sample(rotations.get(uuid), time)
            );
        }

        private Vec3 sample(List<Keyframe> keyframes, double time) {
            if (keyframes == null || keyframes.isEmpty()) {
                return Vec3.ZERO;
            }
            if (keyframes.size() == 1 || length <= 0.0D) {
                return keyframes.get(0).value;
            }

            Keyframe previous = keyframes.get(keyframes.size() - 1);
            Keyframe next = keyframes.get(0);
            double previousTime = previous.time - length;
            double nextTime = next.time;

            for (int i = 0; i < keyframes.size(); i++) {
                Keyframe current = keyframes.get(i);
                Keyframe following = keyframes.get((i + 1) % keyframes.size());
                double followingTime = following.time;
                if (i == keyframes.size() - 1) {
                    followingTime += length;
                }

                if (time >= current.time && time <= followingTime) {
                    previous = current;
                    next = following;
                    previousTime = current.time;
                    nextTime = followingTime;
                    break;
                }
            }

            double span = nextTime - previousTime;
            if (span <= 0.0D) {
                return previous.value;
            }

            double progress = (time - previousTime) / span;
            return previous.value.lerp(next.value, progress);
        }
    }

    private record Transform(Vec3 position, Vec3 rotation) {
    }

    private static class Keyframe {
        private final double time;
        private final Vec3 value;

        private Keyframe(double time, Vec3 value) {
            this.time = time;
            this.value = value;
        }

        private static Keyframe fromJson(JsonObject json) {
            JsonObject data = json.getAsJsonArray("data_points").get(0).getAsJsonObject();
            return new Keyframe(json.get("time").getAsDouble(), Vec3.fromJson(data));
        }
    }

    private static class Vec3 {
        private static final Vec3 ZERO = new Vec3(0.0D, 0.0D, 0.0D);

        private final double x;
        private final double y;
        private final double z;

        private Vec3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static Vec3 fromJson(JsonElement json) {
            if (json == null || json.isJsonNull()) {
                return ZERO;
            }
            if (json.isJsonArray()) {
                JsonArray array = json.getAsJsonArray();
                return new Vec3(
                        array.get(0).getAsDouble(),
                        array.get(1).getAsDouble(),
                        array.get(2).getAsDouble()
                );
            }
            if (json.isJsonObject()) {
                JsonObject object = json.getAsJsonObject();
                return new Vec3(
                        parseDouble(object.get("x").getAsString()),
                        parseDouble(object.get("y").getAsString()),
                        parseDouble(object.get("z").getAsString())
                );
            }

            String[] values = json.getAsString().trim().split("\\s+");
            if (values.length < 3) {
                return ZERO;
            }
            return new Vec3(parseDouble(values[0]), parseDouble(values[1]), parseDouble(values[2]));
        }

        private Vec3 add(Vec3 other) {
            return new Vec3(x + other.x, y + other.y, z + other.z);
        }

        private Vec3 lerp(Vec3 other, double progress) {
            return new Vec3(
                    x + (other.x - x) * progress,
                    y + (other.y - y) * progress,
                    z + (other.z - z) * progress
            );
        }

        private static double parseDouble(String value) {
            try {
                return Double.parseDouble(value.trim().replace(',', '.').toLowerCase(Locale.ROOT));
            } catch (NumberFormatException ignored) {
                return 0.0D;
            }
        }
    }
}
