package net.fishinghacks.utils.common.connection.packets;

import com.google.gson.annotations.SerializedName;

import java.nio.file.Path;

public enum CosmeticType {
    @SerializedName("cape") Cape, @SerializedName("model_preview") ModelPreview,
    @SerializedName("model_data") ModelData, @SerializedName("model_texture") ModelTexture;

    public Path getPath(Path directory, String name) {
        return directory.resolve(subdirectory()).resolve(name + extension());
    }

    public String subdirectory() {
        return switch (this) {
            case Cape -> "capes";
            case ModelPreview, ModelData, ModelTexture -> "models";
        };
    }

    public String extension() {
        return switch (this) {
            case Cape, ModelTexture -> ".png";
            case ModelPreview -> ".preview.png";
            case ModelData -> ".jpm";
        };
    }
}
