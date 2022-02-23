package model.utils;

import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class TextFormattersFactory {
    public static TextFormatter makeIntegerFormatter(Integer range) {
        var builder = new StringBuilder("^(\\s*|[1-9]|");
        if (range < 100) {
            builder.append("[1-").append(range / 10 - 1).append("][0-9]|");
            builder.append("[").append(range / 10).append("][0-").append(range % 10).append("])$");
        } else {
            builder.append("[1-9][0-9]|");
            builder.append("[1-").append(range / 100 - 1).append("][0-").append(range % 100 / 10).append("][0-9]|");
            builder.append(range / 10).append("[0-").append(range % 10).append("])$");
        }
        Pattern patternInteger = Pattern.compile(builder.toString());
        return new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> patternInteger.matcher(change.getControlNewText()).matches() ? change : null);
    }

    public static TextFormatter makeIntegerFormatter() {
        Pattern patternInteger = Pattern.compile("\\d*");
        return new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> patternInteger.matcher(change.getControlNewText()).matches() ? change : null);
    }

    public static TextFormatter makeDoubleFormatter() {
        Pattern patternDouble = Pattern.compile("\\d*|\\d+[,.]\\d*");
        return new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> patternDouble.matcher(change.getControlNewText()).matches() ? change : null);
    }

    public static TextFormatter makeDoubleFormatterWithRange() {
        Pattern patternDouble = Pattern.compile("^([0][,.]([0-9]{1,2}))|(1.0)|(0)|(0.)$");
        return new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> patternDouble.matcher(change.getControlNewText()).matches() ? change : null);
    }
}
