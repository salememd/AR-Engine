package arengine.models;

import arengine.models.File;
import arengine.models.ImageData;

public class Resource {

    public long id;
    public PreviewType type;
    public File templateFile;
    public File previewFile;
    public File resourceFile;
    public ImageData templateData;

    /**
     *
     * @param id
     * @param type
     * @param templateFile
     * @param previewFile
     * @param resourceFile
     */
    public Resource(long id, PreviewType type, File templateFile, File previewFile, File resourceFile) {
        this.id = id;
        this.type = type;
        this.templateFile = templateFile;
        this.previewFile = previewFile;
        this.resourceFile = resourceFile;
    }

    public void setTemplateData(ImageData templateData) {
        this.templateData = templateData;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Resource other = (Resource) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public enum PreviewType {
        IMAGE(0),
        SOUND(1),
        VIDEO(2);

        private final int type;

        /**
         *
         * @param levelCode
         */
        private PreviewType(int type) {
            this.type = type;
        }

        public int getValue() {
            return type;
        }

        /**
         *
         * @param val
         */
        public static PreviewType valueOf(int val) {
            switch (val) {
                case 0:
                    return PreviewType.IMAGE;
                case 1:
                    return PreviewType.SOUND;
                case 2:
                    return PreviewType.VIDEO;
                default:
                    return null;
            }
        }

    }

}
