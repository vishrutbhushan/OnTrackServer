package com.project.onTrackServer.Models;

public class EntityFactory {
    public static BaseEntity createEntity(String type) {
        switch (type.toLowerCase()) {
            case "platform":
                return new Platform();
            case "category":
                return new Category();
            default:
                throw new IllegalArgumentException("Unknown entity type: " + type);
        }
    }
}
