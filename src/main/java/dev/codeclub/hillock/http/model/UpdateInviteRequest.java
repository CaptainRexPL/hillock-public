package dev.codeclub.hillock.http.model;

import dev.codeclub.hillock.database.model.Invite;

public class UpdateInviteRequest {
    private String invite;
    private Long userid;
    private Boolean redeemed;

    public UpdateInviteRequest() {}

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

    public Invite toDbInvite() {
        Invite dbModel = new Invite();
        dbModel.setInvite(this.invite);
        dbModel.setUserid(this.userid);
        dbModel.setRedeemed(this.redeemed);
        return dbModel;
    }
}
