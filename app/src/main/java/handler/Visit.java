package handler;

/**
 * Created by Jakub on 2015-08-14.
 */
public class Visit {
    Long id;
    Long dataBeg;
    Long dataEnd;
    String purpose;
    Long patient;
    String doctor;
    Long googleCalendarId;


    public Visit() {
        this.id = 0l;
        this.dataBeg = 0l;
        this.dataEnd = 0l;
        this.purpose = "";
        this.doctor = "";
        this.patient = 0l;
        this.googleCalendarId = 0l;
    }

    public Visit(Long id, Long dataBeg, Long dataEnd, String purpose, Long patient, String doctor, Long googleCalendarId) {
        this.id = id;
        this.dataBeg = dataBeg;
        this.dataEnd = dataEnd;
        this.purpose = purpose;
        this.doctor = doctor;
        this.patient = patient;
        this.googleCalendarId = googleCalendarId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDataBeg() {
        return dataBeg;
    }

    public void setDataBeg(Long dataBeg) {
        this.dataBeg = dataBeg;
    }

    public Long getDataEnd() {
        return dataEnd;
    }

    public void setDataEnd(Long dataEnd) {
        this.dataEnd = dataEnd;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public Long getPatient() {
        return patient;
    }

    public void setPatient(Long patient) {
        this.patient = patient;
    }

    public Long getGoogleCalendarId() {
        return googleCalendarId;
    }

    public void setGoogleCalendarId(Long googleCalendarId) {
        this.googleCalendarId = googleCalendarId;
    }

    @Override
    public String toString() {
        return "Visit{" +
                "id=" + id +
                ", dataBeg=" + CalendarHandler.milisToFullDate(dataBeg) +
                ", dataEnd=" + CalendarHandler.milisToFullDate(dataEnd) +
                ", purpose='" + purpose + '\'' +
                ", patient=" + patient +
                ", doctor='" + doctor + '\'' +
                ", googleCalendarId=" + googleCalendarId +
                '}';
    }
}
