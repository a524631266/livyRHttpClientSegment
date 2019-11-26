package com.hw.transmitlayer.entity;

public enum FeatureType {
    One_1("1_1",0),One_2("1_2",1),One_2_1("1_2_1",2),
    One_2_2("1_2_2",3),One_2_3("1_2_3",4),One_3("1_3",5),
    One_4("1_4",6),One_4_1("1_4_1",7),One_5("1_5",8),
    One_6("1_6",9),One_7("1_7",10),One_8("1_8",11),
    One_8_1("1_8_1",12),One_9("1_9",13),
    Two_1("2_1",14),
    Two_1_1("2_1_1",15),
    Two_1_2("2_1_2",16),
    Two_2("2_2",17),
    Two_3("2_3",18),
    Two_4("2_4",19),
    Two_5("2_5",20),
    Two_6("2_6",21),
    Three_1("3_1",22),
    Three_2("3_2",23),
    Three_3("3_3",24),
    Four_1("4_1",25),
    Four_2("4_2",26),
    Four_3("4_3",27),
    Four_4("4_4",28),
    Four_5("4_5",29),
    Four_6("4_6",30),
    Five_1("5_1",31),
    Five_1_1("5_1_1",32),
    Five_2("5_2",33),
    Five_2_1("5_2_1",34),
    Five_2_2("5_2_2",35),
    Five_3("5_3",36),
    Five_3_1("5_3_1",37),
    Five_3_2("5_3_2",38),
    Five_3_3("5_3_3",39),
    Five_3_4("5_3_4",40),
    Five_4("5_4",41),
    Five_5("5_5",42),
    Five_6("5_6",43),
    Five_7("5_7",44),
    Five_8("5_8",45),
    Five_9("5_9",46),
    Five_10("5_10",47),
    Six_1("6_1",48),
    Six_2("6_2",49),
    Six_3("6_3",50);
    private String name;
    private int id;
    FeatureType(String name, int id) {
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
