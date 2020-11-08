package com.jit.webchat.securityutils;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.security.*;

public class SignatureUtils {
    private static String algorithm = "SHA1withRSA";

    /**
     * 校验数字签名
     * @param input 原文
     * @param publicKey 公钥key
     * @param signatureData 签名数据
     * @return
     */
    public static boolean verifySignature(String input, PublicKey publicKey, String signatureData) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        // 获取签名对象
        Signature signature = Signature.getInstance(algorithm);
        // 初始化验证签名
        signature.initVerify(publicKey);
        // 传入原文
        signature.update(input.getBytes());
        // 校验数据
        return signature.verify(Base64.decode(signatureData));
    }

    /**
     * 生成数字签名
     * @param input 原文
     * @param privateKey 私钥key
     * @return
     */
    public static String getSignature(String input, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        // 获取签名对象
        Signature signature = Signature.getInstance(algorithm);
        // 初始化签名
        signature.initSign(privateKey);
        // 传入原文
        signature.update(input.getBytes());
        // 开始签名
        byte[] sign = signature.sign();
        return Base64.encode(sign);
    }
}
