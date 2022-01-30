package model.utils;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class TextFormattersFactory {

    public static TextFormatter makeIntegerFormatter() {
        Pattern patternInteger = Pattern.compile("\\d*");
        return new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> {
            return patternInteger.matcher(change.getControlNewText()).matches() ? change : null;
        });
    }

    public static TextFormatter makeDoubleFormatter() {
        Pattern patternDouble = Pattern.compile("\\d*|\\d+\\,\\d*");
        return new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> {
            return patternDouble.matcher(change.getControlNewText()).matches() ? change : null;
        });
    }
}
