package com.internship.recommendation_service.constant;

import com.internship.authentication_library.config.RequestMatcherInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
@ConfigurationProperties("security.paths")
@Getter
@Setter
@NoArgsConstructor
public class PathPermissionConstants {

    private List<RequestMatcherInfo> permittedRequestsForAllUsers = Collections.emptyList();
    private List<RequestMatcherInfo> permittedRequestForSuperAdmin = Collections.emptyList();
    private List<RequestMatcherInfo> permittedRequestsForAdminOrSuperAdmin = Collections.emptyList();
    private List<RequestMatcherInfo> permittedRequestsForUsersOrAdminOrSuperAdmin = Collections.emptyList();
}
