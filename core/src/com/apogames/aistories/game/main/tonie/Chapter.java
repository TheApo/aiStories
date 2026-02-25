package com.apogames.aistories.game.main.tonie;

public class Chapter {
    private String id;
    private String file;
    private String title;
    private float seconds;
    private boolean transcoding;

    public Chapter() {
    }

    public String getId() {
        return this.id;
    }

    public String getFile() {
        return this.file;
    }

    public String getTitle() {
        return this.title;
    }

    public float getSeconds() {
        return this.seconds;
    }

    public boolean isTranscoding() {
        return this.transcoding;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSeconds(float seconds) {
        this.seconds = seconds;
    }

    public void setTranscoding(boolean transcoding) {
        this.transcoding = transcoding;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Chapter)) {
            return false;
        } else {
            Chapter other = (Chapter)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (Float.compare(this.getSeconds(), other.getSeconds()) != 0) {
                return false;
            } else if (this.isTranscoding() != other.isTranscoding()) {
                return false;
            } else {
                label52: {
                    Object this$id = this.getId();
                    Object other$id = other.getId();
                    if (this$id == null) {
                        if (other$id == null) {
                            break label52;
                        }
                    } else if (this$id.equals(other$id)) {
                        break label52;
                    }

                    return false;
                }

                Object this$file = this.getFile();
                Object other$file = other.getFile();
                if (this$file == null) {
                    if (other$file != null) {
                        return false;
                    }
                } else if (!this$file.equals(other$file)) {
                    return false;
                }

                Object this$title = this.getTitle();
                Object other$title = other.getTitle();
                if (this$title == null) {
                    if (other$title != null) {
                        return false;
                    }
                } else if (!this$title.equals(other$title)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof Chapter;
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + Float.floatToIntBits(this.getSeconds());
        result = result * 59 + (this.isTranscoding() ? 79 : 97);
        Object $id = this.getId();
        result = result * 59 + ($id == null ? 43 : $id.hashCode());
        Object $file = this.getFile();
        result = result * 59 + ($file == null ? 43 : $file.hashCode());
        Object $title = this.getTitle();
        result = result * 59 + ($title == null ? 43 : $title.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getId();
        return "Chapter(id=" + var10000 + ", file=" + this.getFile() + ", title=" + this.getTitle() + ", seconds=" + this.getSeconds() + ", transcoding=" + this.isTranscoding() + ")";
    }
}
