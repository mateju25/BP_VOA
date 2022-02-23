package controllers.components;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.shape.Line;

//this code is inspired by https://stackoverflow.com/questions/56222474/how-to-fix-the-location-of-my-linecharts-crosshair
public class CrossHairLineChart<X, Y> extends LineChart<Integer, Double> {

    private final Line vLine;
    private final Line hLine;
    private Group plotArea;
    private final double tickSize = 5;
    private final BooleanProperty showCrossHair = new SimpleBooleanProperty();

    public CrossHairLineChart(Axis<Integer> xAxis, Axis<Double> yAxis, ToggleButton toggleButton) {
        super(xAxis, yAxis);
        vLine = new Line();
        vLine.setStrokeWidth(2);
        hLine = new Line();
        hLine.setStrokeWidth(2);
        toggleButton.setOnAction(event ->
                showCrossHair.set(toggleButton.isSelected()));

        hLine.endYProperty().bind(hLine.startYProperty());
        vLine.endXProperty().bind(vLine.startXProperty());
        vLine.visibleProperty().bind(showCrossHair);
        hLine.visibleProperty().bind(showCrossHair);
        setOnMouseMoved(e -> {
            if (plotArea != null && showCrossHair.get()) {
                Bounds b = plotArea.getBoundsInLocal();
                if (b.getMinX() < e.getX() && e.getX() < b.getMaxX() && b.getMinY() < e.getY() && e.getY() < b.getMaxY()) {
                    moveCrossHair(e.getX() - b.getMinX() - tickSize, e.getY() - b.getMinY() - tickSize);
                }
            }
        });
    }

    private void moveCrossHair(double x, double y) {
        vLine.setStartX(x);
        hLine.setStartY(y);
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        if (plotArea == null && !getPlotChildren().isEmpty()) {
            Group plotContent = (Group) getPlotChildren().get(0).getParent();
            plotArea = (Group) plotContent.getParent();
        }
        if (!getPlotChildren().contains(vLine)) {
            getPlotChildren().addAll(vLine, hLine);
        }
        hLine.setStartX(0);
        hLine.setEndX(getBoundsInLocal().getWidth());

        vLine.setStartY(0);
        vLine.setEndY(getBoundsInLocal().getHeight());
    }
}