package com.wizpanda.file

import com.wizpanda.file.exception.FileUploadException
import com.wizpanda.file.exception.InvalidFileGroupException
import com.wizpanda.file.service.UploaderService
import org.springframework.web.multipart.MultipartFile

import javax.annotation.PostConstruct

class FileUploadService {

    private static Map<String, UploaderService> services = [:]

    StoredFile save(MultipartFile multipartFile, String groupName) throws FileUploadException {
        // TODO Add check for validating group name

        return services.get(groupName).instance().save(multipartFile)
    }

    StoredFile save(File file, String groupName) throws FileUploadException {
        // TODO Add check for validating group name

        return services.get(groupName).instance().save(file)
    }

    @PostConstruct
    void verifyConfig() {
        //log.debug "Verifying all service"
        println "Verifying all service"

        ConfigHelper.allGroups.each { Map.Entry groupConfigEntry ->
            String groupName = groupConfigEntry.key.toString()
            Map groupConfigValue = groupConfigEntry.value

            if (!groupConfigValue.service) {
                throw new InvalidFileGroupException("The service API missing for [${groupName}]")
            }

            Class<? extends UploaderService> serviceClass = groupConfigValue.service

            UploaderService service
            try {
                service = serviceClass.newInstance(groupName, groupConfigValue)
            } catch (IllegalAccessException | InstantiationException | RuntimeException e) {
                log.error "Error creating uploader service object", e
                throw new InvalidFileGroupException("Error while creating the uploader service object");
            }

            services[service.groupName] = service
        }
    }
}