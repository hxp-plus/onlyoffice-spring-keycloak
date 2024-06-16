/**
 * (c) Copyright Ascensio System SIA 2024
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onlyoffice.integration.controllers;

import com.onlyoffice.integration.documentserver.serializers.FilterState;
import com.onlyoffice.integration.documentserver.storage.FileStorageMutator;
import com.onlyoffice.integration.documentserver.storage.FileStoragePathBuilder;
import com.onlyoffice.integration.documentserver.util.Misc;
import com.onlyoffice.integration.documentserver.util.file.FileUtility;
import com.onlyoffice.integration.entities.User;
import com.onlyoffice.integration.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.onlyoffice.integration.documentserver.util.Constants.ANONYMOUS_USER_ID;

@CrossOrigin("*")
@Controller
public class IndexController {

    @Autowired
    private FileStorageMutator storageMutator;

    @Autowired
    private FileStoragePathBuilder storagePathBuilder;

    @Autowired
    private FileUtility fileUtility;

    @Autowired
    private Misc mistUtility;

    @Autowired
    private UserServices userService;

    @Value("${files.docservice.url.site}")
    private String docserviceSite;

    @Value("${files.docservice.url.preloader}")
    private String docservicePreloader;

    @Value("${url.converter}")
    private String urlConverter;

    @Value("${url.editor}")
    private String urlEditor;

    @Value("${server.version}")
    private String serverVersion;
    @Autowired
    private com.onlyoffice.integration.documentserver.models.filemodel.User user;

    @GetMapping("${url.index}")
    public String index(
            @RequestParam(value = "directUrl", required = false, defaultValue = "false") final Boolean directUrl,
            final Model model) {

        // 获取Keycloak的登录信息
        String userFullName = "匿名用户";
        String userSub = ANONYMOUS_USER_ID;
        String userEmail = "anonymous@example.com";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof DefaultOidcUser) {
            DefaultOidcUser userDetails = (DefaultOidcUser) authentication.getPrincipal();
            userFullName = userDetails.getFullName();
            userSub = userDetails.getSubject();
            userEmail = userDetails.getEmail();
        }
        // 如果用户不存在，则创建
        User newUser = userService.createUser(userSub, userFullName, userEmail,
                List.of(FilterState.NULL.toString()),
                "", List.of(FilterState.NULL.toString()), List.of(FilterState.NULL.toString()),
                List.of(FilterState.NULL.toString()), List.of(FilterState.NULL.toString()),
                List.of(FilterState.NULL.toString()), null, true, true, false);

        java.io.File[] files = storageMutator.getStoredFiles(); // get all the stored files from the storage
        List<String> docTypes = new ArrayList<>();
        List<Boolean> filesEditable = new ArrayList<>();
        List<String> versions = new ArrayList<>();
        List<Boolean> isFillFormDoc = new ArrayList<>();

        for (java.io.File file : files) { // run through all the files
            String fileName = file.getName(); // get file name
            // add a document type of each file to the list
            docTypes.add(fileUtility.getDocumentType(fileName).toString().toLowerCase());
            // specify if a file is editable or not
            filesEditable.add(fileUtility.getEditedExts().contains(fileUtility.getFileExtension(fileName)));
            // add a file version to the list
            versions.add(" [" + storagePathBuilder.getFileVersion(fileName, true) + "]");
            isFillFormDoc.add(fileUtility.getFillExts().contains(fileUtility.getFileExtension(fileName)));
        }

        // add all the parameters to the model
        model.addAttribute("isFillFormDoc", isFillFormDoc);
        model.addAttribute("versions", versions);
        model.addAttribute("files", files);
        model.addAttribute("docTypes", docTypes);
        model.addAttribute("filesEditable", filesEditable);
        model.addAttribute("datadocs", docserviceSite + docservicePreloader);
        model.addAttribute("username", newUser.getName());
        model.addAttribute("directUrl", directUrl);
        model.addAttribute("serverVersion", serverVersion);

        return "index.html";
    }

    @PostMapping("/config")
    @ResponseBody
    public HashMap<String, String> configParameters() { // get configuration parameters
        HashMap<String, String> configuration = new HashMap<>();
        // put a list of the extensions that can be filled to config
        configuration.put("FillExtList", String.join(",", fileUtility.getFillExts()));
        // put a list of the extensions that can be converted to config
        configuration.put("ConverExtList", String.join(",", fileUtility.getConvertExts()));
        // put a list of the extensions that can be edited to config
        configuration.put("EditedExtList", String.join(",", fileUtility.getEditedExts()));
        configuration.put("UrlConverter", urlConverter);
        configuration.put("UrlEditor", urlEditor);

        return configuration;
    }
}
