package com.apogames.aistories.game.main.tonie;

public class Household {
    private String id;
    private String name;
    private String image;
    private boolean foreignCreativeTonieContent;
    private String access;
    private boolean canLeave;
    private String ownerName;

    public Household() {
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getImage() {
        return this.image;
    }

    public boolean isForeignCreativeTonieContent() {
        return this.foreignCreativeTonieContent;
    }

    public String getAccess() {
        return this.access;
    }

    public boolean isCanLeave() {
        return this.canLeave;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setForeignCreativeTonieContent(boolean foreignCreativeTonieContent) {
        this.foreignCreativeTonieContent = foreignCreativeTonieContent;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public void setCanLeave(boolean canLeave) {
        this.canLeave = canLeave;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Household)) {
            return false;
        } else {
            Household other = (Household)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.isForeignCreativeTonieContent() != other.isForeignCreativeTonieContent()) {
                return false;
            } else if (this.isCanLeave() != other.isCanLeave()) {
                return false;
            } else {
                label76: {
                    Object this$id = this.getId();
                    Object other$id = other.getId();
                    if (this$id == null) {
                        if (other$id == null) {
                            break label76;
                        }
                    } else if (this$id.equals(other$id)) {
                        break label76;
                    }

                    return false;
                }

                Object this$name = this.getName();
                Object other$name = other.getName();
                if (this$name == null) {
                    if (other$name != null) {
                        return false;
                    }
                } else if (!this$name.equals(other$name)) {
                    return false;
                }

                label62: {
                    Object this$image = this.getImage();
                    Object other$image = other.getImage();
                    if (this$image == null) {
                        if (other$image == null) {
                            break label62;
                        }
                    } else if (this$image.equals(other$image)) {
                        break label62;
                    }

                    return false;
                }

                label55: {
                    Object this$access = this.getAccess();
                    Object other$access = other.getAccess();
                    if (this$access == null) {
                        if (other$access == null) {
                            break label55;
                        }
                    } else if (this$access.equals(other$access)) {
                        break label55;
                    }

                    return false;
                }

                Object this$ownerName = this.getOwnerName();
                Object other$ownerName = other.getOwnerName();
                if (this$ownerName == null) {
                    if (other$ownerName != null) {
                        return false;
                    }
                } else if (!this$ownerName.equals(other$ownerName)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof Household;
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + (this.isForeignCreativeTonieContent() ? 79 : 97);
        result = result * 59 + (this.isCanLeave() ? 79 : 97);
        Object $id = this.getId();
        result = result * 59 + ($id == null ? 43 : $id.hashCode());
        Object $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        Object $image = this.getImage();
        result = result * 59 + ($image == null ? 43 : $image.hashCode());
        Object $access = this.getAccess();
        result = result * 59 + ($access == null ? 43 : $access.hashCode());
        Object $ownerName = this.getOwnerName();
        result = result * 59 + ($ownerName == null ? 43 : $ownerName.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getId();
        return "Household(id=" + var10000 + ", name=" + this.getName() + ", image=" + this.getImage() + ", foreignCreativeTonieContent=" + this.isForeignCreativeTonieContent() + ", access=" + this.getAccess() + ", canLeave=" + this.isCanLeave() + ", ownerName=" + this.getOwnerName() + ")";
    }
}