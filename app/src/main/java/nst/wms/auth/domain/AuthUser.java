package nst.wms.auth.domain;

public class AuthUser {

    private String providerUserId;
    private String email;
    private String name;
    private String avatarUrl;

    public AuthUser() {
    }

    public AuthUser(String providerUserId, String email, String name, String avatarUrl) {
        this.providerUserId = providerUserId;
        this.email = email;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
