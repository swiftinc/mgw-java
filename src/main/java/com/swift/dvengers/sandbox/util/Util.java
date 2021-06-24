package com.swift.dvengers.sandbox.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Base64;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.HashUtil;
import org.jose4j.lang.JoseException;

public class Util {
	public static String sign(String jwsPayloadStr, String sharedKey) throws JoseException {
		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(jwsPayloadStr);
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
		jws.setHeader("typ", "JWT");
		Key key = new HmacKey(sharedKey.getBytes());
		jws.setKey(key);
		jws.sign();
		return jws.getCompactSerialization();
	}

	public static String buildPayload(String busAppName, String profileId, String path, Object payload) {
		JwtClaims claims = new JwtClaims();

		claims.setGeneratedJwtId();
		claims.setIssuer(busAppName);
		claims.setClaim("profileId", profileId);
		claims.setIssuedAtToNow();
		claims.setExpirationTimeMinutesInTheFuture(30);
		claims.setClaim("absPath", path);
		String digest = calculateDigestValue(payload);
		if (digest != null) {
			claims.setClaim("digest", digest);
		}
		return claims.toJson();
	}

	public static String calculateDigestValue(Object data) {
		String digestValue = null;
		if (data != null) {
			MessageDigest md = HashUtil.getMessageDigest("SHA-256");
			digestValue = Base64.getUrlEncoder()
					.encodeToString(md.digest(data.toString().getBytes(StandardCharsets.UTF_8)));
		}
		return digestValue;
	}

	public static boolean verifyResponse(String responseBody, String jwsToken, String sharedKey) {
		boolean retval = false;

		try {
			JsonWebSignature jws = new JsonWebSignature();
			jws.setCompactSerialization(jwsToken.substring("Bearer ".length()));

			JwtClaims jwtClaims = JwtClaims.parse(jws.getUnverifiedPayload());
			if ((jwtClaims.getClaimValue("digest") != null)
					&& !jwtClaims.getClaimValue("digest").equals(calculateDigestValue(responseBody))) {
				System.out.println("Response payload digest is valid\n");
			} else {
				System.out.println("Response payload digest is invalid\n");
				return false;
			}

			jws.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
					AlgorithmIdentifiers.HMAC_SHA256));
			Key key = new HmacKey(((String) sharedKey).getBytes(StandardCharsets.UTF_8));
			jws.setKey(key);

			retval = jws.verifySignature();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return retval;
	}
}
