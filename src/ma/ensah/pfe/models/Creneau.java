package ma.ensah.pfe.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Creneau {
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    public Creneau(LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Creneau creneau = (Creneau) o;
        return Objects.equals(date, creneau.date) && Objects.equals(heureDebut, creneau.heureDebut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, heureDebut);
    }

    @Override
    public String toString() {
        return date.toString() + " [" + heureDebut.toString() + " - " + heureFin.toString() + "]";
    }
}
