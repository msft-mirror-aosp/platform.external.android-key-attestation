/* Copyright 2019, The Android Open Source Project, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.attestation;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.android.attestation.CertificateRevocationStatus.Reason;
import com.google.android.attestation.CertificateRevocationStatus.Status;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test for {@link CertificateRevocationStatus}.
 */
@RunWith(JUnit4.class)
public final class CertificateRevocationStatusTest {

  private static final String TEST_STATUS_LIST_PATH = "src/test/resources/status.json";

  // Certificate generated by TestDPC with RSA Algorithm and StrongBox Security Level
  private static final String TEST_CERT =
      "-----BEGIN CERTIFICATE-----\n"
          + "MIIB8zCCAXqgAwIBAgIRAMxm6ak3E7bmQ7JsFYeXhvcwCgYIKoZIzj0EAwIwOTEM"
          + "MAoGA1UEDAwDVEVFMSkwJwYDVQQFEyA0ZjdlYzg1N2U4MDU3NDdjMWIxZWRhYWVm"
          + "ODk1NDk2ZDAeFw0xOTA4MTQxOTU0MTBaFw0yOTA4MTExOTU0MTBaMDkxDDAKBgNV"
          + "BAwMA1RFRTEpMCcGA1UEBRMgMzJmYmJiNmRiOGM5MTdmMDdhYzlhYjZhZTQ4MTAz"
          + "YWEwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQzg+sx9lLrkNIZwLYZerzL1bPK"
          + "2zi75zFEuuI0fIr35DJND1B4Z8RPZ3djzo3FOdAObqvoZ4CZVxcY3iQ1ffMMo2Mw"
          + "YTAdBgNVHQ4EFgQUzZOUqhJOO7wttSe9hYemjceVsgIwHwYDVR0jBBgwFoAUWlnI"
          + "9iPzasns60heYXIP+h+Hz8owDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMC"
          + "AgQwCgYIKoZIzj0EAwIDZwAwZAIwUFz/AKheCOPaBiRGDk7LaSEDXVYmTr0VoU8T"
          + "bIqrKGWiiMwsGEmW+Jdo8EcKVPIwAjAoO7n1ruFh+6mEaTAukc6T5BW4MnmYadkk"
          + "FSIjzDAaJ6lAq+nmmGQ1KlZpqi4Z/VI=\n"
          + "-----END CERTIFICATE-----";

  @Test
  public void loadTestSerial() throws Exception {
    CertificateFactory factory = CertificateFactory.getInstance("X509");
    X509Certificate cert =
        (X509Certificate)
            factory.generateCertificate(new ByteArrayInputStream(TEST_CERT.getBytes(UTF_8)));
    BigInteger serialNumber = cert.getSerialNumber();
    CertificateRevocationStatus statusEntry = CertificateRevocationStatus
        .loadStatusFromFile(serialNumber, TEST_STATUS_LIST_PATH);
    assertThat(statusEntry).isNotNull();
    assertThat(statusEntry.status).isEqualTo(Status.SUSPENDED);
    assertThat(statusEntry.reason).isEqualTo(Reason.KEY_COMPROMISE);
  }

  @Test
  public void loadBadSerial() throws Exception {
    assertThat(CertificateRevocationStatus.loadStatusFromFile("badbeef", TEST_STATUS_LIST_PATH))
        .isNull();
    assertThat(CertificateRevocationStatus.loadStatusFromFile(BigInteger.valueOf(0xbadbeef), TEST_STATUS_LIST_PATH))
        .isNull();
  }

  @Test
  public void loadAllTestEntries() throws Exception {
    HashSet<String> allTestSerialNumbers = new HashSet<>();
    allTestSerialNumbers.add("6681152659205225093");
    allTestSerialNumbers.add("8350192447815228107");
    allTestSerialNumbers.add("9408173275444922801");
    allTestSerialNumbers.add("11244410301401252959");
    allTestSerialNumbers.add("cc66e9a93713b6e643b26c15879786f7");

    HashMap<String, CertificateRevocationStatus> statusMap =
            CertificateRevocationStatus.loadAllEntriesFromFile(TEST_STATUS_LIST_PATH);

    assertThat(statusMap.keySet()).isEqualTo(allTestSerialNumbers);
    assertThat(statusMap.get("8350192447815228107").status)
            .isEqualTo(CertificateRevocationStatus.Status.REVOKED);
    assertThat(statusMap.get("8350192447815228107").reason)
            .isEqualTo(CertificateRevocationStatus.Reason.KEY_COMPROMISE);
    assertThat(statusMap.get("cc66e9a93713b6e643b26c15879786f7").status)
            .isEqualTo(CertificateRevocationStatus.Status.SUSPENDED);
    assertThat(statusMap.get("cc66e9a93713b6e643b26c15879786f7").reason)
            .isEqualTo(CertificateRevocationStatus.Reason.KEY_COMPROMISE);
    assertThat(statusMap.get("cc66e9a93713b6e643b26c15879786f7").comment)
            .isEqualTo("Entry for testing only");
  }
}
