package com.hw.transmitlayer.entity;

public enum FaultType {
    Unbalance("三相不平衡",1),OVERLOAD("电流过载",2)
    ,SHORT("短路",3),BadEnv("环境不良",4),LOOSE("虚接",5)
    ,Leak("漏电",6),MixNE("零地混接",7),MIXNN("零线混用",8)
    ,OpenN("零线断路",9),Harmonic("谐波",10),SensorFault("传感器故障",11)
    ,DeviceStartUp("大功率设备启动",12);
    private String name;
    private int id;
    FaultType(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
