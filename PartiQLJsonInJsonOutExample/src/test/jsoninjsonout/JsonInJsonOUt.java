package jsoninjsonout;

import com.amazon.ion.IonDatagram;
import com.amazon.ion.IonReader;
import com.amazon.ion.IonSystem;
import com.amazon.ion.IonWriter;
import com.amazon.ion.system.IonReaderBuilder;
import com.amazon.ion.system.IonSystemBuilder;
import com.amazon.ion.system.IonTextWriterBuilder;
import org.junit.Test;
import org.partiql.lang.CompilerPipeline;
import org.partiql.lang.eval.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class JsonInJsonOUt {

    private static final IonSystem ION = IonSystemBuilder.standard().build();
    private final CompilerPipeline pipeline = CompilerPipeline.standard(ION);
    protected ExprValueFactory valueFactory = pipeline.getValueFactory();
    private static String QUERY =
            "SELECT d.msg.device.id, d.msg.\"key\" " +
            "FROM data AS d " +
            "WHERE msg.\"key\" = 's10'";
    private static String DATA = "{\"msg\":{\n" +
            "\"device\" :{\n" +
            "\"id\" : \"SerialXYZ1123\"\n" +
            "},\n" +
            "\"key\" : \"s10\",\n" +
            "\"time\" : 1939492934\n" +
            "}\n" +
            "}";

    @Test
    public void test() throws IOException {

        // Compile the query
        Expression expr = pipeline.compile(QUERY);
        // Turn JSON into ExprValue
        ExprValue table = valueFactory.newFromIonValue(ION.newLoader().load(DATA));


        Map<String, ExprValue> globals = new HashMap<>();
        // create table named data
        globals.put("data", valueFactory.newList(table));

        EvaluationSession session = EvaluationSession.builder().globals(Bindings.ofMap(globals)).build();

        ExprValue ionOutput = expr.eval(session);
        System.out.println("Ion : " +  ionOutput.getIonValue().toPrettyString());
        // toJson taken from http://amzn.github.io/ion-docs/guides/cookbook.html#down-converting-to-json
        StringBuilder stringBuilder = new StringBuilder();
        try (IonWriter jsonWriter = IonTextWriterBuilder.json().withPrettyPrinting().build(stringBuilder)) {
            ionOutput.getIonValue();
            rewrite(ionOutput.getIonValue().toString(), jsonWriter);
        }
        System.out.println("JSON: " + stringBuilder.toString());

    }

    void rewrite(String textIon, IonWriter writer) throws IOException {
        IonReader reader = IonReaderBuilder.standard().build(textIon);
        writer.writeValues(reader);
    }
}
