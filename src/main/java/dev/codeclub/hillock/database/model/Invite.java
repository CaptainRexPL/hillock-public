package dev.codeclub.hillock.database.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "invites", schema = "public")
public class Invite {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invites_id_gen")
    @SequenceGenerator(name = "invites_id_gen", sequenceName = "invites_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "invite", nullable = false, length = 25)
    private String invite;

    @Column(name = "userid", nullable = false)
    private Long userid;

    @ColumnDefault("false")
    @Column(name = "redeemed", nullable = false)
    private Boolean redeemed = false;

    public Invite() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvite() {
        return invite;
    }

    public void setInvite(String invite) {
        this.invite = invite;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public Boolean getRedeemed() {
        return redeemed;
    }

    public void setRedeemed(Boolean redeemed) {
        this.redeemed = redeemed;
    }

    public Invite(String invite, Long userid, Boolean redeemed) {
        this.invite = invite;
        this.userid = userid;
        this.redeemed = redeemed;
    }
}