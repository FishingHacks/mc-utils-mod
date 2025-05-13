package net.fishinghacks.utils.client.cosmetics;

public enum CosmeticAttachment {
    Head, LeftArm, RightArm, LeftLeg, RightLeg, Body;

    public static CosmeticAttachment fromString(String value) throws CosmeticModelLoader.InvalidModelException {
        return switch (value) {
            case "head" -> CosmeticAttachment.Head;
            case "body" -> CosmeticAttachment.Body;
            case "left_leg" -> CosmeticAttachment.LeftLeg;
            case "right_leg" -> CosmeticAttachment.RightLeg;
            case "left_arm" -> CosmeticAttachment.LeftArm;
            case "right_arm" -> CosmeticAttachment.RightArm;
            default -> throw new CosmeticModelLoader.InvalidModelException("invalid value for attachTo: " + value);
        };
    }
}
