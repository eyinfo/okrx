package io.github.eyinfo.okrxtest.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VersionBody extends Response<VersionBody> {
    private String versionName;
    private String version;
    private int buildNumber;
    private int minBuildNumber;
    private String content;
    private String enableDownload;
    private String downloadUrl;
    private String externalUrl;
}
