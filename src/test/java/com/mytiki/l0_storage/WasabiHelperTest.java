/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.l0_storage;

import com.mytiki.l0_storage.utilities.SHAHelper;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WasabiHelperTest {

    @Test
    public void Test_Policy_Signature_Pair_Success() throws NoSuchAlgorithmException, InvalidKeyException {
        String policy = "{ \"expiration\": \"2015-12-30T12:00:00.000Z\",\r\n" +
                "  \"conditions\": [\r\n" +
                "    {\"bucket\": \"sigv4examplebucket\"},\r\n" +
                "    [\"starts-with\", \"$key\", \"user/user1/\"],\r\n" +
                "    {\"acl\": \"public-read\"},\r\n" +
                "    {\"success_action_redirect\": \"http://sigv4examplebucket.s3.amazonaws.com/successful_upload.html\"},\r\n" +
                "    [\"starts-with\", \"$Content-Type\", \"image/\"],\r\n" +
                "    {\"x-amz-meta-uuid\": \"14365123651274\"},\r\n" +
                "    {\"x-amz-server-side-encryption\": \"AES256\"},\r\n" +
                "    [\"starts-with\", \"$x-amz-meta-tag\", \"\"],\r\n" +
                "\r\n" +
                "    {\"x-amz-credential\": \"AKIAIOSFODNN7EXAMPLE/20151229/us-east-1/s3/aws4_request\"},\r\n" +
                "    {\"x-amz-algorithm\": \"AWS4-HMAC-SHA256\"},\r\n" +
                "    {\"x-amz-date\": \"20151229T000000Z\" }\r\n" +
                "  ]\r\n" +
                "}";

        String b64p = Base64.getEncoder().encodeToString(policy.getBytes(StandardCharsets.UTF_8));
        assertEquals(b64p, "eyAiZXhwaXJhdGlvbiI6ICIyMDE1LTEyLTMwVDEyOjAwOjAwLjAwMFoiLA0KICAiY29uZGl0aW9ucyI6IFsNCiAgICB7ImJ1Y2tldCI6ICJzaWd2NGV4YW1wbGVidWNrZXQifSwNCiAgICBbInN0YXJ0cy13aXRoIiwgIiRrZXkiLCAidXNlci91c2VyMS8iXSwNCiAgICB7ImFjbCI6ICJwdWJsaWMtcmVhZCJ9LA0KICAgIHsic3VjY2Vzc19hY3Rpb25fcmVkaXJlY3QiOiAiaHR0cDovL3NpZ3Y0ZXhhbXBsZWJ1Y2tldC5zMy5hbWF6b25hd3MuY29tL3N1Y2Nlc3NmdWxfdXBsb2FkLmh0bWwifSwNCiAgICBbInN0YXJ0cy13aXRoIiwgIiRDb250ZW50LVR5cGUiLCAiaW1hZ2UvIl0sDQogICAgeyJ4LWFtei1tZXRhLXV1aWQiOiAiMTQzNjUxMjM2NTEyNzQifSwNCiAgICB7IngtYW16LXNlcnZlci1zaWRlLWVuY3J5cHRpb24iOiAiQUVTMjU2In0sDQogICAgWyJzdGFydHMtd2l0aCIsICIkeC1hbXotbWV0YS10YWciLCAiIl0sDQoNCiAgICB7IngtYW16LWNyZWRlbnRpYWwiOiAiQUtJQUlPU0ZPRE5ON0VYQU1QTEUvMjAxNTEyMjkvdXMtZWFzdC0xL3MzL2F3czRfcmVxdWVzdCJ9LA0KICAgIHsieC1hbXotYWxnb3JpdGhtIjogIkFXUzQtSE1BQy1TSEEyNTYifSwNCiAgICB7IngtYW16LWRhdGUiOiAiMjAxNTEyMjlUMDAwMDAwWiIgfQ0KICBdDQp9");

        byte[] secret = ("AWS4" + "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY").getBytes(StandardCharsets.UTF_8);
        byte[] dateKey = SHAHelper.hmacSha256("20151229", secret);
        byte[] regionKey = SHAHelper.hmacSha256("us-east-1", dateKey);
        byte[] serviceKey = SHAHelper.hmacSha256("s3", regionKey);
        byte[] signingKey = SHAHelper.hmacSha256("aws4_request", serviceKey);

        String sig =  Hex.encodeHexString(SHAHelper.hmacSha256(b64p, signingKey));
        assertEquals(sig, "8afdbf4008c03f22c2cd3cdb72e4afbb1f6a588f3255ac628749a66d7f09699e");
    }
}