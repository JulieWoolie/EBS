package me.julie.ebs.type;

import me.julie.ebs.EbsTypeRegistry;
import me.julie.ebs.element.EbsCompound;
import me.julie.ebs.element.EbsElement;
import me.julie.ebs.element.EbsElements;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

public class EbsCompoundType implements EbsType<EbsCompound> {
    static final EbsCompoundType INSTANCE = new EbsCompoundType();

    public static EbsCompoundType getInstance() {
        return INSTANCE;
    }

    // ---------------------------------------
    //         Compound IO format
    // ---------------------------------------
    //
    // The compound follows a very simple IO format:
    // The first 4 bytes are the size integer.
    // The rest of the format follows the pattern
    // of UTF string (entry key); entry type ID; entry value.
    // This pattern repeats as many times as the
    // size states
    //

    @Override
    public EbsCompound read(EbsTypeRegistry registry, DataInput input) throws IOException {
        // Read the size
        int size = input.readInt();
        EbsCompound compound = EbsElements.newCompound(size);

        // If the size is bigger than 0
        if(size > 0) {
            for (int i = 0; i < size; i++) {
                String key = input.readUTF();
                EbsType type = registry.readType(input);

                EbsElement element = type.read(registry, input);
                compound.put(key, element);
            }
        }

        return compound;
    }

    @Override
    public void write(EbsTypeRegistry registry, DataOutput output, EbsCompound val) throws IOException {
        // Write the size
        output.writeInt(val.size());

        // Write every element
        for (Map.Entry<String, EbsElement> e: val.entrySet()) {
            EbsType type = e.getValue().getType();

            // Compound standard: key; type; value
            output.writeUTF(e.getKey());

            registry.writeType(type, output);
            type.write(registry, output, e.getValue());
        }
    }
}