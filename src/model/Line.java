package model;

import java.util.Objects;

public class Line {
    private final Dot d1;
    private final Dot d2;
    private boolean occupied = false;

    public Line(Dot a, Dot b) {
        // d1 vai ser sempre o ponto "menor" para evitar duplicados invertidos
        if (a.getX() < b.getX() || (a.getX() == b.getX() && a.getY() < b.getY())) {
            this.d1 = a;
            this.d2 = b;
        } else {
            this.d1 = b;
            this.d2 = a;
        }
    }

    public boolean isOccupied() { return occupied; }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public Dot getDot1(){
        return d1;
    }

    public Dot getDot2(){
        return d2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return Objects.equals(d1, line.d1) && Objects.equals(d2, line.d2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(d1, d2);
    }

    @Override
    public String toString() {
        return d1 + "-" + d2;
    }
}