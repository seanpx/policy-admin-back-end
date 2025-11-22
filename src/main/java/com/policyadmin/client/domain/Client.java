package com.policyadmin.client.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

@Entity
@Table(name = "clnt_main", uniqueConstraints = @UniqueConstraint(columnNames = {"clntid_typ", "clntid_no"}))
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clntnum")
    private Long clntnum;

    @Column(name = "surname")
    private String surname;

    @Column(name = "givname")
    private String givname;

    @Column(name = "cltdob")
    private LocalDate cltdob;

    @Column(name = "cltsex")
    private String cltsex;

    @Column(name = "clntid_typ", nullable = false)
    private String clntidTyp;

    @Column(name = "clntid_no", nullable = false)
    private String clntidNo;

    protected Client() {
        // for JPA
    }

    public Client(String surname, String givname, LocalDate cltdob, String cltsex, String clntidTyp, String clntidNo) {
        this.surname = surname;
        this.givname = givname;
        this.cltdob = cltdob;
        this.cltsex = cltsex;
        this.clntidTyp = clntidTyp;
        this.clntidNo = clntidNo;
    }

    public Long getClntnum() {
        return clntnum;
    }

    public void setClntnum(Long clntnum) {
        this.clntnum = clntnum;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGivname() {
        return givname;
    }

    public void setGivname(String givname) {
        this.givname = givname;
    }

    public LocalDate getCltdob() {
        return cltdob;
    }

    public void setCltdob(LocalDate cltdob) {
        this.cltdob = cltdob;
    }

    public String getCltsex() {
        return cltsex;
    }

    public void setCltsex(String cltsex) {
        this.cltsex = cltsex;
    }

    public String getClntidTyp() {
        return clntidTyp;
    }

    public void setClntidTyp(String clntidTyp) {
        this.clntidTyp = clntidTyp;
    }

    public String getClntidNo() {
        return clntidNo;
    }

    public void setClntidNo(String clntidNo) {
        this.clntidNo = clntidNo;
    }
}
