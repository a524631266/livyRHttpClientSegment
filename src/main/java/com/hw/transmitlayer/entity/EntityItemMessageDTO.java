package com.hw.transmitlayer.entity;

import com.hw.base.model.EntityModel;


public class EntityItemMessageDTO {
    private EntityModel entityModel;
    private FaultType faultType;
    private FeatureType featureType;

    public EntityItemMessageDTO(EntityModel entityModel, FaultType faultType, FeatureType featureType) {
        this.entityModel = entityModel;
        this.faultType = faultType;
        this.featureType = featureType;
    }

    public EntityModel getEntityModel() {
        return entityModel;
    }

    public FaultType getFaultType() {
        return faultType;
    }

    public FeatureType getFeatureType() {
        return featureType;
    }
}
