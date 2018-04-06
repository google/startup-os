package com.google.startupos.proto_vs_json;

import com.google.startupos.common.FileUtils;
import com.google.startupos.proto_vs_json.Protos.Person;
import com.google.startupos.proto_vs_json.Protos.Person.PizzaTopping;
import com.google.startupos.proto_vs_json.Protos.Book;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;


/* A simple tool to compare proto and json formats */
public class ProtoVsJsonTool {
    @FlagDesc(
        name = "prototxt_output",
        description = "Path to prototxt output file")
    private static final Flag<String> prototxtOutput =
            Flag.create("example.prototxt");

    @FlagDesc(
        name = "proto_binary_output",
        description = "Path to proto binary output file")
    private static final Flag<String> protoBinaryOutput =
            Flag.create("example.protobin");

    @FlagDesc(
        name = "json_output",
        description = "Path to json output file")
    private static final Flag<String> jsonOutput =
            Flag.create("example.json");

    public static void main(String[] args) throws IOException {
        Iterable<String> packages
                = ImmutableList.of(ProtoVsJsonTool.class.getPackage().getName());
        Flags.parse(args, packages);

        Person person = Person.newBuilder()
            .setName("John Smith")
            .setFavoriteBeatlesSong("Hey Jude")
            .setLuckyNumber(7)
            .addFavoritePizzaTopping(PizzaTopping.MUSHROOMS)
            .addFavoritePizzaTopping(PizzaTopping.BLACK_OLIVES)
            .addFavoritePizzaTopping(PizzaTopping.GREEN_PEPPERS)
            .addBookRead(Book.newBuilder().setName("To Kill a Mockingbird").setAuthor("Harper Lee").build())
            .addBookRead(Book.newBuilder().setName("Lord of the Flies").setAuthor("William Golding").build())
            .build();

        FileUtils.writeProtoBinary(person, protoBinaryOutput.get());
        FileUtils.writePrototxt(person, prototxtOutput.get());
        FileUtils.writeString(JsonFormat.printer().print(person), jsonOutput.get());
    }
}