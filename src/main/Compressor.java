package main;

import com.idealista.fpe.FormatPreservingEncryption;
import com.idealista.fpe.builder.FormatPreservingEncryptionBuilder;
import com.idealista.fpe.config.Alphabet;
import com.idealista.fpe.config.GenericDomain;
import com.idealista.fpe.config.GenericTransformations;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class Compressor {

    //TODO: add description and remove

    private static FormatPreservingEncryption formatPreservingEncryption;

    private static void initializeEncryption() {
        char[] alphabet = new char[96];
        for (int i = 0; i < alphabet.length; i++) {
            alphabet[i] = (char) (i + 32);
        }
        formatPreservingEncryption = FormatPreservingEncryptionBuilder
                .ff1Implementation()
                .withDomain(new GenericDomain(new Alphabet() {
                    @Override
                    public char[] availableCharacters() {
                        return alphabet;
                    }

                    @Override
                    public Integer radix() {
                        return alphabet.length;
                    }
                }, new GenericTransformations(alphabet), new GenericTransformations(alphabet)))
                .withDefaultPseudoRandomFunction("the alchemy game".getBytes())
                .withDefaultLengthRange()
                .build();
    }

    private static String encrypt(String string) {
        return formatPreservingEncryption.encrypt(string, new byte[0]);
    }

    private static String decrypt(String string) {
        return formatPreservingEncryption.decrypt(string, new byte[0]);
    }

    public static void toBinary(JSONArray array) {
        initializeEncryption();
        try {
            MessagePacker packer = MessagePack.newDefaultPacker(new FileOutputStream("elements.bin"));
            packer.packString("elements");
            packer.packArrayHeader(array.size());
            for (int i = 0; i < array.size(); i++) {
                JSONObject object = array.getJSONObject(i);
                packer.packString(encrypt("name"));
                packer.packString(encrypt(object.getString("name")));
                packer.packString(encrypt("group"));
                packer.packString(encrypt(object.getString("group")));

                packer.packString(encrypt("combos"));
                JSONArray combos = object.getJSONArray("combos");
                packer.packArrayHeader(combos.size());
                for (int j = 0; j < combos.size(); j++) {
                    JSONObject combo = combos.getJSONObject(j);
                    if (combo.hasKey("first element")) {
                        packer.packString(encrypt("first element"));
                        packer.packString(encrypt(combo.getString("first element")));
                        packer.packString(encrypt("second element"));
                        packer.packString(encrypt(combo.getString("second element")));
                    } else if (combo.hasKey("elements")) {
                        packer.packString(encrypt("elements"));
                        JSONArray elements = combo.getJSONArray("elements");
                        packer.packArrayHeader(elements.size());
                        for (int k = 0; k < elements.size(); k++) {
                            packer.packString(encrypt(elements.getString(k)));
                        }
                    } else if (combo.hasKey("first elements")) {
                        packer.packString(encrypt("first elements"));
                        JSONArray firstElements = combo.getJSONArray("first elements");
                        packer.packArrayHeader(firstElements.size());
                        for (int k = 0; k < firstElements.size(); k++) {
                            packer.packString(encrypt(firstElements.getString(k)));
                        }
                        packer.packString(encrypt("second elements"));
                        JSONArray secondElements = combo.getJSONArray("second elements");
                        packer.packArrayHeader(secondElements.size());
                        for (int k = 0; k < secondElements.size(); k++) {
                            packer.packString(encrypt(secondElements.getString(k)));
                        }
                    }
                }

                if (object.hasKey("tags")) {
                    packer.packString(encrypt("tags"));
                    JSONArray tags = object.getJSONArray("tags");
                    packer.packArrayHeader(tags.size());
                    for (int j = 0; j < tags.size(); j++) {
                        packer.packString(encrypt(tags.getString(j)));
                    }
                }
            }
            packer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void fromBinary(File file) {
        if (formatPreservingEncryption == null) {
            initializeEncryption();
        }
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);
            System.out.println(unpacker.unpackString());
            int size = unpacker.unpackArrayHeader();
            String name = decrypt(unpacker.unpackString());
            for (int i = 0; i < size; i++) {
                System.out.println(name); //name
                System.out.println(decrypt(unpacker.unpackString()));

                System.out.println(decrypt(unpacker.unpackString())); //group
                System.out.println(decrypt(unpacker.unpackString()));

                System.out.println(decrypt(unpacker.unpackString())); //combos
                int combos = unpacker.unpackArrayHeader();
                for (int j = 0; j < combos; j++) {
                    String string = decrypt(unpacker.unpackString());
                    switch (string) {
                        case "first element":
                            System.out.println(decrypt(unpacker.unpackString()));
                            System.out.println(decrypt(unpacker.unpackString())); //second element

                            System.out.println(decrypt(unpacker.unpackString()));
                            break;
                        case "elements": {
                            int elements = unpacker.unpackArrayHeader();
                            for (int k = 0; k < elements; k++) {
                                System.out.println(decrypt(unpacker.unpackString()));
                            }
                            break;
                        }
                        case "first elements": {
                            int elements = unpacker.unpackArrayHeader();
                            for (int k = 0; k < elements; k++) {
                                System.out.println(decrypt(unpacker.unpackString()));
                            }
                            System.out.println(decrypt(unpacker.unpackString())); //second elements

                            elements = unpacker.unpackArrayHeader();
                            for (int k = 0; k < elements; k++) {
                                System.out.println(decrypt(unpacker.unpackString()));
                            }
                            break;
                        }
                    }
                }

                //decrypting optional keys

                //check if we have reached the end of file
                if (!unpacker.hasNext()) {
                    break;
                }

                String key = decrypt(unpacker.unpackString());
                if (key.equals("tags")) {
                    System.out.println(key);
                    int tags = unpacker.unpackArrayHeader();
                    for (int j = 0; j < tags; j++) {
                        System.out.println(decrypt(unpacker.unpackString()));
                    }
                }

                name = key.equals("name") ? key : unpacker.unpackString();
            }
            unpacker.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
