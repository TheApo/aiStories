package com.apogames.aistories.game.main.tonie;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreativeTonie {
    private String id;
    private String name;
    private boolean live;
    private boolean isPrivate;
    private String imageUrl;
    private Object[] transcodingErrors;
    private boolean transcoding;
    private float secondsPresent;
    private float secondsRemaining;
    private int chaptersPresent;
    private int chaptersRemaining;
    private Chapter[] chapters;
    private String householdId;
    @JsonIgnore
    private Household household;
    @JsonIgnore
    private RequestHandler requestHandler;

    @JsonIgnore
    public Chapter findChapterByTitle(String title) {
        Chapter[] var2 = this.chapters;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Chapter chapter = var2[var4];
            if (StringUtils.equals(chapter.getTitle(), title)) {
                return chapter;
            }
        }

        return null;
    }

    @JsonIgnore
    public void deleteChapter(Chapter chapter) {
        List<Chapter> chapters = new ArrayList();
        Chapter[] var3 = this.chapters;
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Chapter chapterIter = var3[var5];
            if (!StringUtils.equals(chapterIter.getId(), chapter.getId())) {
                chapters.add(chapterIter);
            }
        }

        this.chapters = (Chapter[])chapters.toArray(new Chapter[0]);
    }

    @JsonIgnore
    public void uploadFile(String title, String path) throws IOException {
        this.requestHandler.uploadFile(this, new File(path), title);
    }

    @JsonIgnore
    public void commit() throws IOException {
        this.requestHandler.commitTonie(this);
    }

    @JsonIgnore
    public void refresh() throws IOException {
        CreativeTonie tmp = this.requestHandler.refreshTonie(this);
        this.setId(tmp.getId());
        this.setName(tmp.getName());
        this.setLive(tmp.isLive());
        this.setPrivate(tmp.isPrivate);
        this.setImageUrl(tmp.getImageUrl());
        this.setTranscoding(tmp.isTranscoding());
        this.setSecondsPresent(tmp.getSecondsPresent());
        this.setSecondsRemaining((float)tmp.getChaptersRemaining());
        this.setChaptersPresent(tmp.getChaptersPresent());
        this.setChaptersRemaining(tmp.getChaptersRemaining());
        this.setChapters(tmp.getChapters());
        this.setHouseholdId(tmp.getHouseholdId());
    }

    public CreativeTonie() {
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isLive() {
        return this.live;
    }

    public boolean isPrivate() {
        return this.isPrivate;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public Object[] getTranscodingErrors() {
        return this.transcodingErrors;
    }

    public boolean isTranscoding() {
        return this.transcoding;
    }

    public float getSecondsPresent() {
        return this.secondsPresent;
    }

    public float getSecondsRemaining() {
        return this.secondsRemaining;
    }

    public int getChaptersPresent() {
        return this.chaptersPresent;
    }

    public int getChaptersRemaining() {
        return this.chaptersRemaining;
    }

    public Chapter[] getChapters() {
        return this.chapters;
    }

    public String getHouseholdId() {
        return this.householdId;
    }

    public Household getHousehold() {
        return this.household;
    }

    public RequestHandler getRequestHandler() {
        return this.requestHandler;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTranscodingErrors(Object[] transcodingErrors) {
        this.transcodingErrors = transcodingErrors;
    }

    public void setTranscoding(boolean transcoding) {
        this.transcoding = transcoding;
    }

    public void setSecondsPresent(float secondsPresent) {
        this.secondsPresent = secondsPresent;
    }

    public void setSecondsRemaining(float secondsRemaining) {
        this.secondsRemaining = secondsRemaining;
    }

    public void setChaptersPresent(int chaptersPresent) {
        this.chaptersPresent = chaptersPresent;
    }

    public void setChaptersRemaining(int chaptersRemaining) {
        this.chaptersRemaining = chaptersRemaining;
    }

    public void setChapters(Chapter[] chapters) {
        this.chapters = chapters;
    }

    public void setHouseholdId(String householdId) {
        this.householdId = householdId;
    }

    @JsonIgnore
    public void setHousehold(Household household) {
        this.household = household;
    }

    @JsonIgnore
    public void setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof CreativeTonie)) {
            return false;
        } else {
            CreativeTonie other = (CreativeTonie)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.isLive() != other.isLive()) {
                return false;
            } else if (this.isPrivate() != other.isPrivate()) {
                return false;
            } else if (this.isTranscoding() != other.isTranscoding()) {
                return false;
            } else if (Float.compare(this.getSecondsPresent(), other.getSecondsPresent()) != 0) {
                return false;
            } else if (Float.compare(this.getSecondsRemaining(), other.getSecondsRemaining()) != 0) {
                return false;
            } else if (this.getChaptersPresent() != other.getChaptersPresent()) {
                return false;
            } else if (this.getChaptersRemaining() != other.getChaptersRemaining()) {
                return false;
            } else {
                label108: {
                    Object this$id = this.getId();
                    Object other$id = other.getId();
                    if (this$id == null) {
                        if (other$id == null) {
                            break label108;
                        }
                    } else if (this$id.equals(other$id)) {
                        break label108;
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

                Object this$imageUrl = this.getImageUrl();
                Object other$imageUrl = other.getImageUrl();
                if (this$imageUrl == null) {
                    if (other$imageUrl != null) {
                        return false;
                    }
                } else if (!this$imageUrl.equals(other$imageUrl)) {
                    return false;
                }

                if (!Arrays.deepEquals(this.getTranscodingErrors(), other.getTranscodingErrors())) {
                    return false;
                } else if (!Arrays.deepEquals(this.getChapters(), other.getChapters())) {
                    return false;
                } else {
                    Object this$householdId = this.getHouseholdId();
                    Object other$householdId = other.getHouseholdId();
                    if (this$householdId == null) {
                        if (other$householdId != null) {
                            return false;
                        }
                    } else if (!this$householdId.equals(other$householdId)) {
                        return false;
                    }

                    Object this$household = this.getHousehold();
                    Object other$household = other.getHousehold();
                    if (this$household == null) {
                        if (other$household != null) {
                            return false;
                        }
                    } else if (!this$household.equals(other$household)) {
                        return false;
                    }

                    Object this$requestHandler = this.getRequestHandler();
                    Object other$requestHandler = other.getRequestHandler();
                    if (this$requestHandler == null) {
                        if (other$requestHandler != null) {
                            return false;
                        }
                    } else if (!this$requestHandler.equals(other$requestHandler)) {
                        return false;
                    }

                    return true;
                }
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof CreativeTonie;
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + (this.isLive() ? 79 : 97);
        result = result * 59 + (this.isPrivate() ? 79 : 97);
        result = result * 59 + (this.isTranscoding() ? 79 : 97);
        result = result * 59 + Float.floatToIntBits(this.getSecondsPresent());
        result = result * 59 + Float.floatToIntBits(this.getSecondsRemaining());
        result = result * 59 + this.getChaptersPresent();
        result = result * 59 + this.getChaptersRemaining();
        Object $id = this.getId();
        result = result * 59 + ($id == null ? 43 : $id.hashCode());
        Object $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        Object $imageUrl = this.getImageUrl();
        result = result * 59 + ($imageUrl == null ? 43 : $imageUrl.hashCode());
        result = result * 59 + Arrays.deepHashCode(this.getTranscodingErrors());
        result = result * 59 + Arrays.deepHashCode(this.getChapters());
        Object $householdId = this.getHouseholdId();
        result = result * 59 + ($householdId == null ? 43 : $householdId.hashCode());
        Object $household = this.getHousehold();
        result = result * 59 + ($household == null ? 43 : $household.hashCode());
        Object $requestHandler = this.getRequestHandler();
        result = result * 59 + ($requestHandler == null ? 43 : $requestHandler.hashCode());
        return result;
    }

    public String toString() {
        String var10000 = this.getId();
        return "CreativeTonie(id=" + var10000 + ", name=" + this.getName() + ", live=" + this.isLive() + ", isPrivate=" + this.isPrivate() + ", imageUrl=" + this.getImageUrl() + ", transcodingErrors=" + Arrays.deepToString(this.getTranscodingErrors()) + ", transcoding=" + this.isTranscoding() + ", secondsPresent=" + this.getSecondsPresent() + ", secondsRemaining=" + this.getSecondsRemaining() + ", chaptersPresent=" + this.getChaptersPresent() + ", chaptersRemaining=" + this.getChaptersRemaining() + ", chapters=" + Arrays.deepToString(this.getChapters()) + ", householdId=" + this.getHouseholdId() + ", household=" + this.getHousehold() + ", requestHandler=" + this.getRequestHandler() + ")";
    }
}
