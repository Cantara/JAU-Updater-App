package no.cantara.emi;

public class JauProperties {
    public final String JAU_ARTIFACT_URL;
    public final String JAU_ARTIFACT_CHECKSUM;
    public final String NEW_JAU_LOCATION;
    public final String JAU_FOLDER_NAME;
    public final String FILES_TO_COPY;


    public JauProperties(String jauArtifact, String jauArtifactChecksum, String newJauLocation, String jauFolderName, String filesToCopy) {
        JAU_ARTIFACT_URL = jauArtifact;
        JAU_ARTIFACT_CHECKSUM = jauArtifactChecksum;
        NEW_JAU_LOCATION = newJauLocation;
        JAU_FOLDER_NAME = jauFolderName;
        FILES_TO_COPY = filesToCopy;
    }
}
