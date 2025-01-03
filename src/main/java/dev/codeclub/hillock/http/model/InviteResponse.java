package dev.codeclub.hillock.http.model;

import dev.codeclub.hillock.database.model.Invite;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

public class InviteResponse {
    private Long id;
    private String invite;
    private Long userid;
    private Boolean redeemed;

    public InviteResponse() {

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

    public static InviteResponse FromDbInvite(Invite dbModel) {

        InviteResponse response = new InviteResponse();
        response.id = dbModel.getId();
        response.invite = dbModel.getInvite();
        response.userid = dbModel.getUserid();
        response.redeemed = dbModel.getRedeemed();
        return response;
    }
}
