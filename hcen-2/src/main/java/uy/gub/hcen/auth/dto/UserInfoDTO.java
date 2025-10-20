package uy.gub.hcen.auth.dto;

/**
 * DTO representing user information included in authentication responses.
 */
public class UserInfoDTO {

    private String ci;
    private String inusId;
    private String firstName;
    private String lastName;
    private String role;

    // Constructors

    public UserInfoDTO() {
    }

    public UserInfoDTO(String ci, String inusId, String firstName, String lastName, String role) {
        this.ci = ci;
        this.inusId = inusId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Getters and Setters

    public String getCi() {
        return ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public String getInusId() {
        return inusId;
    }

    public void setInusId(String inusId) {
        this.inusId = inusId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UserInfoDTO{" +
                "ci='" + ci + '\'' +
                ", inusId='" + inusId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
