package io.pivotal.security.data;

import io.pivotal.security.auth.UserContext;
import io.pivotal.security.credential.CertificateCredentialValue;
import io.pivotal.security.domain.CertificateCredentialVersion;
import io.pivotal.security.domain.CredentialVersion;
import io.pivotal.security.exceptions.EntryNotFoundException;
import io.pivotal.security.exceptions.ParameterizedValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.pivotal.security.request.PermissionOperation.READ;

@Component
public class CertificateAuthorityService {

  private final CredentialVersionDataService credentialVersionDataService;
  private PermissionDataService permissionService;

  @Autowired
  public CertificateAuthorityService(CredentialVersionDataService credentialVersionDataService, PermissionDataService permissionService) {
    this.credentialVersionDataService = credentialVersionDataService;
    this.permissionService = permissionService;
  }

  public CertificateCredentialValue findMostRecent(UserContext userContext, String caName) {
    if (!permissionService.hasPermission(userContext.getActor(), caName, READ)) {
      throw new EntryNotFoundException("error.credential.invalid_access");
    }

    CredentialVersion mostRecent = credentialVersionDataService.findMostRecent(caName);
    if (!(mostRecent instanceof CertificateCredentialVersion)) {
      throw new ParameterizedValidationException("error.not_a_ca_name");
    }
    CertificateCredentialVersion certificateCredential = (CertificateCredentialVersion) mostRecent;

    if (!certificateCredential.getParsedCertificate().isCa()) {
      throw new ParameterizedValidationException("error.cert_not_ca");
    }

    return new CertificateCredentialValue(null, certificateCredential.getCertificate(),
        certificateCredential.getPrivateKey(), null);
  }
}
