package off.kys.backtalk.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utility for AES encryption/decryption using PBKDF2 for key derivation.
 */
object CryptoUtils {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val ITERATION_COUNT = 65536
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 16

    /**
     * Encrypts the [plainText] using the [password].
     * Result is formatted as base64(salt + iv + ciphertext).
     */
    fun encrypt(plainText: String, password: CharArray): String {
        val encryptedBytes = encryptInternal(plainText.toByteArray(), password)
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    /**
     * Encrypts the [data] using the [password].
     * Result is salt + iv + ciphertext.
     */
    fun encrypt(data: ByteArray, password: CharArray): ByteArray {
        return encryptInternal(data, password)
    }

    private fun encryptInternal(data: ByteArray, password: CharArray): ByteArray {
        val salt = ByteArray(SALT_LENGTH).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(IV_LENGTH).apply { SecureRandom().nextBytes(this) }

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))

        val cipherText = cipher.doFinal(data)

        return salt + iv + cipherText
    }

    /**
     * Decrypts the [encryptedText] using the [password].
     */
    fun decrypt(encryptedText: String, password: CharArray): String {
        val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
        return String(decrypt(combined, password))
    }

    /**
     * Decrypts the [encryptedData] using the [password].
     */
    fun decrypt(encryptedData: ByteArray, password: CharArray): ByteArray {
        val salt = encryptedData.sliceArray(0 until SALT_LENGTH)
        val iv = encryptedData.sliceArray(SALT_LENGTH until SALT_LENGTH + IV_LENGTH)
        val cipherText = encryptedData.sliceArray(SALT_LENGTH + IV_LENGTH until encryptedData.size)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

        return cipher.doFinal(cipherText)
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }
}
