package net.fishinghacks.utils.client.cosmetics;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fishinghacks.utils.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;

/*

# Unsupported fields:
- sprites, attachments (see attachTo), sizeAdd, sizesAdd

# Part:
attachTo: String
-> the part to attach to.
-> valid values: head, leftLeg, rightLeg, leftArm, rightArm, body
-> only on root

textureSize: int[2]
-> has the texture size in [width, height]
-> only on root

id: String
-> the id of the part. optional for the root part.

invertAxis?: String
-> contains the axis to invert (e.g., "xy" inverts the x and y-axis)

translate?: int[3]
-> x, y and z translations

rotate?: int[3]
-> x, y and z angle

boxes?: BoxOffset | BoxUV[]
-> the boxes in this part

submodel?: Part
submodels?: Part[]

# BoxOffset:
textureOffset: int[2]
-> the offset into the texture by x and y. see texture offset to uv for the uv coordinates.
coordinates: int[6]
-> the box position and dimension, specified as [x, y, z, width, height, depth]
mirrorTexture: String
-> the parts of the texture to mirror (u, v or uv)

# BoxUV:
["uvDown" | "uvUp" | "uvNorth" | "uvSouth" | "uvWest" | "uvEast"]: int[4]
-> the uv coordinates for each face specified as [u1, v1, u2, v2].
coordinates: int[6]
-> the box position and dimension, specified as [x, y, z, width, height, depth]
mirrorTexture: String
-> the parts of the texture to mirror (u, v or uv)

# Texture Offset to uv:
Note: the uv coordinates still have to be offset by the specified offset.
up: [depth, 0, depth + width, depth]
down: [depth + width, 0, depth + 2width, depth]
west: [0, depth, depth, depth + height]
north: [depth, depth, depth + width, depth + height]
east: [depth + width, depth, 2depth + width, depth + height]
south: [2depth + width, depth, 2depth + 2width, depth + height]
*/

public class CosmeticModelLoader {
    public static Optional<CosmeticModelPart> loadModel(ResourceLocation location) throws InvalidModelException {
        var resource = Minecraft.getInstance().getResourceManager().getResource(location);
        if (resource.isEmpty()) return Optional.empty();
        JsonObject obj;
        try (var data = resource.get().openAsReader()) {
            obj = new Gson().fromJson(data, JsonElement.class).getAsJsonObject();
        } catch (IOException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new InvalidModelException("Failed to parse json: " + e);
        }
        return Optional.of(loadModel(obj));
    }

    public static CosmeticModelPart loadModel(byte[] bytes) throws InvalidModelException {
        JsonObject obj;
        try {
            obj = new Gson().fromJson(new InputStreamReader(new ByteArrayInputStream(bytes)), JsonElement.class)
                .getAsJsonObject();
        } catch (Exception e) {
            throw new InvalidModelException("Failed to parse json: " + e);
        }
        return loadModel(obj);
    }

    public static CosmeticModelPart loadModel(JsonObject object) throws InvalidModelException {
        int[] textureSize = process(orThrow(object.get("textureSize"), "textureSize is not set"), v -> {
            var arr = v.getAsJsonArray();
            return new int[]{arr.get(0).getAsInt(), arr.get(1).getAsInt()};
        }, "textureSize is not of type int[2]", null);
        return loadModel(object, false, textureSize[0], textureSize[1]).getA();
    }

    private static Pair<CosmeticModelPart, String> loadModel(JsonObject object, boolean needsId, int textureWidth,
                                                             int textureHeight) throws InvalidModelException {
        if (needsId && !object.has("id")) {
            throw new InvalidModelException("part that requires an id does no have one.");
        }
        String id = process(object.get("id"), JsonElement::getAsString, "id was not of type string", "");
        String invertAxis = process(object.get("invertAxis"), JsonElement::getAsString,
            "invertAxis was not of type string", "");
        boolean invertX = invertAxis.contains("x");
        boolean invertY = invertAxis.contains("y");
        boolean invertZ = invertAxis.contains("z");
        int[] translation = process(object.get("translate"), v -> {
            var array = v.getAsJsonArray();
            int x = array.get(0).getAsInt();
            int y = array.get(1).getAsInt();
            int z = array.get(2).getAsInt();
            return new int[]{x, y, z};
        }, "translate was not of type int[3]", new int[]{0, 0, 0});
        int[] rotate = process(object.get("rotate"), v -> {
            var array = v.getAsJsonArray();
            int x = array.get(0).getAsInt();
            int y = array.get(1).getAsInt();
            int z = array.get(2).getAsInt();
            return new int[]{x, y, z};
        }, "rotate was not of type int[3]", new int[]{0, 0, 0});
        List<UnbakedCube> unbakedCubes = new ArrayList<>();
        var boxes = process(object.get("boxes"), JsonElement::getAsJsonArray, "boxes was not of type array", null);
        if (boxes != null) for (int i = 0; i < boxes.size(); ++i) {
            var box = process(boxes.get(i), JsonElement::getAsJsonObject, "boxes was not of type object[]", null);
            unbakedCubes.add(loadCube(box));
        }

        List<Cube> bakedCubes = new ArrayList<>();

        for (var cube : unbakedCubes) {
            int x = cube.x;
            int y = cube.y;
            int z = cube.z;
            if (invertX) x = -x - cube.width;
            if (invertY) y = -y - cube.height;
            if (invertZ) z = -z - cube.depth;

            bakedCubes.add(
                new Cube(x, y, z, cube.width, cube.height, cube.depth, cube.coords, textureWidth, textureHeight,
                    cube.mirrorU, cube.mirrorV));
        }
        if (invertX) translation[0] *= -1;
        if (invertX) rotate[0] *= -1;
        if (invertY) translation[1] *= -1;
        if (invertY) rotate[1] *= -1;
        if (invertZ) translation[2] *= -1;
        if (invertZ) rotate[2] *= -1;

        Map<String, CosmeticModelPart> models = new HashMap<>();
        var submodel = process(object.get("submodel"), JsonElement::getAsJsonObject, "submodel was not of type object",
            null);
        if (submodel != null) {
            var model = loadModel(submodel, true, textureWidth, textureHeight);
            models.put(model.getB(), model.getA());
        }
        var submodels = process(object.get("submodels"), JsonElement::getAsJsonArray, "submodels was not of type array",
            null);
        if (submodels != null) for (int i = 0; i < submodels.size(); ++i) {
            var model = process(submodels.get(i), JsonElement::getAsJsonObject, "submodels was not of type object[]",
                null);
            var modelPart = loadModel(model, true, textureWidth, textureHeight);
            models.put(modelPart.getB(), modelPart.getA());
        }
        CosmeticModelPart part = new CosmeticModelPart(bakedCubes, models);
        part.setInitialPose(PartPose.offsetAndRotation(translation[0], translation[1], translation[2],
            (float) rotate[0] * Mth.DEG_TO_RAD, (float) rotate[1] * Mth.DEG_TO_RAD,
            (float) rotate[2] * Mth.DEG_TO_RAD));
        return new Pair<>(part, id);
    }

    private static UnbakedCube loadCube(JsonObject obj) throws InvalidModelException {
        var coordsArray = process(orThrow(obj.get("coordinates"), "coordinates is not set"),
            JsonElement::getAsJsonArray, "coordinates is not of type array", null);
        int x;
        int y;
        int z;
        int width;
        int height;
        int depth;
        String mirrorTexture = process(obj.get("mirrorTexture"), JsonElement::getAsString,
            "mirrorTexture is not of type string", "");
        try {
            x = coordsArray.get(0).getAsInt();
            y = coordsArray.get(1).getAsInt();
            z = coordsArray.get(2).getAsInt();
            width = coordsArray.get(3).getAsInt();
            height = coordsArray.get(4).getAsInt();
            depth = coordsArray.get(5).getAsInt();
        } catch (Exception ignored) {
            throw new InvalidModelException("coordinates is not of type int[6]");
        }
        var uv = parseCoords(obj, width, height, depth);

        return new UnbakedCube(x, y, z, width, height, depth, uv, mirrorTexture.contains("u"),
            mirrorTexture.contains("v"));
    }

    private static UvCoords parseCoords(JsonObject obj, int width, int height, int depth) throws InvalidModelException {
        var textureOffset = process(obj.get("textureOffset"), JsonElement::getAsJsonArray,
            "textureOffset was not of type array", null);
        if (textureOffset != null) {
            try {
                int offsetX = textureOffset.get(0).getAsInt();
                int offsetY = textureOffset.get(0).getAsInt();
                return UvCoords.fromTextureOffset(offsetX, offsetY, width, height, depth);
            } catch (Exception ignored) {
                throw new InvalidModelException("textureOffset was not of type int[2]");
            }
        }
        var uvNorth = processUv(obj, "uvNorth");
        var uvSouth = processUv(obj, "uvSouth");
        var uvWest = processUv(obj, "uvWest");
        var uvEast = processUv(obj, "uvEast");
        var uvUp = processUv(obj, "uvUp");
        var uvDown = processUv(obj, "uvDown");
        return new UvCoords(uvUp, uvDown, uvNorth, uvSouth, uvWest, uvEast);
    }

    private static int[] processUv(JsonObject obj, String k) throws InvalidModelException {
        var elem = orThrow(obj.get(k), k + " is not set");
        try {
            var array = elem.getAsJsonArray();
            return new int[]{array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt(),
                array.get(3).getAsInt(),};
        } catch (Exception ignored) {
            throw new InvalidModelException(k + " was not of type int[4]");
        }
    }

    private static <R, T> R process(@Nullable T value, Function<T, R> function, String exception,
                                    R default_value) throws InvalidModelException {
        if (value == null) return default_value;
        try {
            return function.apply(value);
        } catch (Exception ignored) {
            throw new InvalidModelException(exception);
        }
    }

    private static <T> @NotNull T orThrow(@Nullable T value, String error) throws InvalidModelException {
        if (value == null) throw new InvalidModelException(error);
        return value;
    }

    public static class InvalidModelException extends Exception {
        public InvalidModelException(String error) {
            super(error);
        }
    }

    private record UvCoords(int[] up, int[] down, int[] north, int[] south, int[] west, int[] east) {
        public static UvCoords fromTextureOffset(int offsetX, int offsetY, int width, int height, int depth) {
            @SuppressWarnings("UnnecessaryLocalVariable") int topY = offsetY;
            int bottomY = offsetY + depth;
            int endY = offsetY + depth + height;
            int upX = offsetX + depth + width;
            int downX = offsetX + depth;
            int topEndX = offsetX + depth + width;
            @SuppressWarnings("UnnecessaryLocalVariable") int westX = offsetX;
            int northX = offsetX + depth;
            int eastX = offsetX + depth + width;
            int southX = offsetX + 2 * depth + width;
            int bottomEndX = offsetX + 2 * depth + 2 * width;
            return new UvCoords(new int[]{upX, topY, topEndX, bottomY}, new int[]{downX, topY, upX, bottomY},
                new int[]{northX, bottomY, eastX, endY}, new int[]{southX, bottomY, bottomEndX, endY},
                new int[]{westX, bottomY, northX, endY}, new int[]{eastX, bottomY, southX, endY});
        }
    }

    private record UnbakedCube(int x, int y, int z, int width, int height, int depth, UvCoords coords, boolean mirrorU,
                               boolean mirrorV) {
    }

    public static class Cube {
        public final Polygon[] polygons;
        public final float x;
        public final float y;
        public final float z;

        private Cube(float x, float y, float z, int width, int height, int depth, UvCoords uv, int textureWidth,
                     int textureHeight, boolean mirrorU, boolean mirrorV) {
            this.polygons = new Polygon[6];
            this.x = x;
            this.y = y;
            this.z = z;

            float maxX = x + width + 0.001f;
            float maxY = y + height + 0.001f;
            float maxZ = z + depth + 0.001f;

            x -= 0.001f;
            y -= 0.001f;
            z -= 0.001f;

            ModelPart.Vertex vertex1 = new ModelPart.Vertex(x, y, z, 0.0F, 0.0F);
            ModelPart.Vertex vertex2 = new ModelPart.Vertex(maxX, y, z, 0.0F, 8.0F);
            ModelPart.Vertex vertex3 = new ModelPart.Vertex(maxX, maxY, z, 8.0F, 8.0F);
            ModelPart.Vertex vertex4 = new ModelPart.Vertex(x, maxY, z, 8.0F, 0.0F);
            ModelPart.Vertex vertex5 = new ModelPart.Vertex(x, y, maxZ, 0.0F, 0.0F);
            ModelPart.Vertex vertex6 = new ModelPart.Vertex(maxX, y, maxZ, 0.0F, 8.0F);
            ModelPart.Vertex vertex7 = new ModelPart.Vertex(maxX, maxY, maxZ, 8.0F, 8.0F);
            ModelPart.Vertex vertex8 = new ModelPart.Vertex(x, maxY, maxZ, 8.0F, 0.0F);

            int i = 0;
            int[] uvs = uv.down;
            Utils.getLOGGER().info("{} {}", mirrorU, mirrorV);
            polygons[i++] = new Polygon(
                mirrorV ? new ModelPart.Vertex[]{vertex1, vertex2, vertex6, vertex5} : new ModelPart.Vertex[]{vertex8,
                    vertex7, vertex3, vertex4}, (float) (mirrorU ? uvs[2] : uvs[0]) / textureWidth,
                (float) (mirrorV ? uvs[3] : uvs[1]) / textureHeight, (float) (mirrorU ? uvs[0] : uvs[2]) / textureWidth,
                (float) (mirrorV ? uvs[1] : uvs[3]) / textureHeight, mirrorV ? Direction.DOWN : Direction.UP);
            uvs = uv.up;
            polygons[i++] = new Polygon(
                mirrorV ? new ModelPart.Vertex[]{vertex8, vertex7, vertex3, vertex4} : new ModelPart.Vertex[]{vertex1,
                    vertex2, vertex6, vertex5}, (float) (mirrorU ? uvs[2] : uvs[0]) / textureWidth,
                (float) (mirrorV ? uvs[3] : uvs[1]) / textureHeight, (float) (mirrorU ? uvs[0] : uvs[2]) / textureWidth,
                (float) (mirrorV ? uvs[1] : uvs[3]) / textureHeight, mirrorV ? Direction.UP : Direction.DOWN);
            uvs = uv.north;
            polygons[i++] = new Polygon(new ModelPart.Vertex[]{vertex2, vertex1, vertex4, vertex3},
                (float) (mirrorU ? uvs[2] : uvs[0]) / textureWidth, (float) (mirrorV ? uvs[3] : uvs[1]) / textureHeight,
                (float) (mirrorU ? uvs[0] : uvs[2]) / textureWidth, (float) (mirrorV ? uvs[1] : uvs[3]) / textureHeight,
                Direction.NORTH);
            uvs = uv.south;
            polygons[i++] = new Polygon(new ModelPart.Vertex[]{vertex5, vertex6, vertex7, vertex8},
                (float) (mirrorU ? uvs[2] : uvs[0]) / textureWidth, (float) (mirrorV ? uvs[3] : uvs[1]) / textureHeight,
                (float) (mirrorU ? uvs[0] : uvs[2]) / textureWidth, (float) (mirrorV ? uvs[1] : uvs[3]) / textureHeight,
                Direction.SOUTH);
            uvs = uv.west;
            polygons[i++] = new Polygon(
                mirrorU ? new ModelPart.Vertex[]{vertex1, vertex5, vertex8, vertex4} : new ModelPart.Vertex[]{vertex6,
                    vertex2, vertex3, vertex7}, (float) (mirrorU ? uvs[2] : uvs[0]) / textureWidth,
                (float) (mirrorV ? uvs[3] : uvs[1]) / textureHeight, (float) (mirrorU ? uvs[0] : uvs[2]) / textureWidth,
                (float) (mirrorV ? uvs[1] : uvs[3]) / textureHeight, mirrorU ? Direction.WEST : Direction.EAST);
            uvs = uv.east;
            polygons[i] = new Polygon(
                mirrorU ? new ModelPart.Vertex[]{vertex6, vertex2, vertex3, vertex7} : new ModelPart.Vertex[]{vertex1,
                    vertex5, vertex8, vertex4}, (float) (mirrorU ? uvs[2] : uvs[0]) / textureWidth,
                (float) (mirrorV ? uvs[3] : uvs[1]) / textureHeight, (float) (mirrorU ? uvs[0] : uvs[2]) / textureWidth,
                (float) (mirrorV ? uvs[1] : uvs[3]) / textureHeight, mirrorU ? Direction.EAST : Direction.WEST);
        }

        public void render(PoseStack.Pose pose, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
            Matrix4f transformation = pose.pose();
            Vector3f pos = new Vector3f();

            for (var polygon : polygons) {
                Vector3f normalized = pose.transformNormal(polygon.normal, pos);
                float normalX = normalized.x();
                float normalY = normalized.y();
                float normalZ = normalized.z();

                for (ModelPart.Vertex vertex : polygon.vertices) {
                    float x = vertex.pos().x() / 16.0F;
                    float y = vertex.pos().y() / 16.0F;
                    float z = vertex.pos().z() / 16.0F;
                    Vector3f position = transformation.transformPosition(x, y, z, pos);
                    buffer.addVertex(position.x(), position.y(), position.z(), color, vertex.u(), vertex.v(),
                        packedOverlay, packedLight, normalX, normalY, normalZ);
                }
            }
        }
    }

    public record Polygon(ModelPart.Vertex[] vertices, Vector3f normal) {
        public Polygon(ModelPart.Vertex[] vertices, float u1, float v1, float u2, float v2, Direction direction) {
            this(vertices, direction.step());
            vertices[0] = vertices[0].remap(u2, v1);
            vertices[1] = vertices[1].remap(u1, v1);
            vertices[2] = vertices[2].remap(u1, v2);
            vertices[3] = vertices[3].remap(u2, v2);
        }
    }
}
