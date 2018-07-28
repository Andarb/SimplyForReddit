package com.github.andarb.simplyreddit.data;

import java.util.List;

public class Preview {
    private List<Images> images;

    private boolean enabled;

    public void setImages(List<Images> images) {
        this.images = images;
    }

    public List<Images> getImages() {
        return this.images;
    }


    public class Images {
        private Source source;

        public void setSource(Source source) {
            this.source = source;
        }

        public Source getSource() {
            return this.source;
        }


        public class Source {
            private String url;

            public void setUrl(String url) {
                this.url = url;
            }

            public String getUrl() {
                return this.url;
            }
        }
    }
}






