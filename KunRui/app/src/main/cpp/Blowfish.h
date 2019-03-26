/**
 * @file Blowfish.h
 * @brief 
 * @author  
 * @date 2009/05/20
 * @version 1.0
 * 
 * BLOWFISH ENCRYPTION ALGORITHM
 * 
 * Encryption and Decryption of Byte Strings using the Blowfish Encryption Algorithm.
 * Blowfish is a block cipher that encrypts data in 8-byte blocks. The algorithm consists
 * of two parts: a key-expansion part and a data-ancryption part. Key expansion converts a
 * variable key of at least 1 and at most 56 bytes into several subkey arrays totaling
 * 4168 bytes. Blowfish has 16 rounds. Each round consists of a key-dependent permutation,
 * and a key and data-dependent substitution. All operations are XORs and additions on 32-bit words.
 * The only additional operations are four indexed array data lookups per round.
 * Blowfish uses a large number of subkeys. These keys must be precomputed before any data
 * encryption or decryption. The P-array consists of 18 32-bit subkeys: P0, P1,...,P17.
 * There are also four 32-bit S-boxes with 256 entries each: S0,0, S0,1,...,S0,255;
 * S1,0, S1,1,...,S1,255; S2,0, S2,1,...,S2,255; S3,0, S3,1,...,S3,255;
 * 
 * The Electronic Code Book (ECB), Cipher Block Chaining (CBC) and Cipher Feedback modes
 * are used:
 *
 * In ECB mode if the same block is encrypted twice with the same key, the resulting
 * ciphertext blocks are the same.
 * 
 * In CBC Mode a ciphertext block is obtained by first xoring the
 * plaintext block with the previous ciphertext block, and encrypting the resulting value.
 *
 * In CFB mode a ciphertext block is obtained by encrypting the previous ciphertext block
 * and xoring the resulting value with the plaintext
 *
 * The previous ciphertext block is usually stored in an Initialization Vector (IV).
 * An Initialization Vector of zero is commonly used for the first block, though other
 * arrangements are also in use.
 */
#ifndef __BLOWFISH_H__
#define __BLOWFISH_H__


/**
 * ���������
 */
#define NONE_ERRROR           	0	//û�д���
#define OPEN_FILE_ERROR       	-1	//���ļ�ʧ��
#define NEW_MEM_ERROR         	-2	//�����ڴ�ʧ��
#define KEY_ERROR         	  	-3	//��Կ����
#define DATA_ERROR				-4	//��ʼ����Box���ݴ���
#define LENGTH_ERROR			-5	//���ȴ���,���ܻ��߽��ܵ����ݳ��Ȳ�����Ҫ��
#define WRITE_FILE_ERROR		-6	//д�ļ�����
#define READ_FILE_ERROR			-7	//���ļ�����
#define FILE_SIZE_ERROR			-8	//�ļ���С����
#define CREATE_FILE_ERROR		-9  //�����ļ�����
#define PADDING_INSTR_ERROR		-10 //������봮����
#define PADDING_PASSWD_ERROR	-11 //������봮����
#define OUT_BUFFER_ERROR		-12	//�ⲿ������ַ��洢�ռ䲻��


/**
 * @fn
 * @brief ��inStr�������,��֤����Ϊ8��������
 * @param inStr
 * @param mode
 * @param outStr ��䴮ָ��
 * @param outStrLen ��䴮�ķ���ռ䳤��; ������ַ�������:Ϊ�����ַ����������ϵ�8����
 * @return 0:�ɹ�����0:ʧ��
 */
int Padding8BytesNoNew(const char* inStr, int mode, char* outStr, unsigned int& outStrLen);


/**
 * @fn
 * @brief ��inStr�������,��֤����Ϊ8��������
 * @param inStr
 * @param mode
 * @param outStrLen ������ַ�������
 * @return ������ַ�ָ��,���ⲿ�������ͷ��ڴ�
 */
char* Padding8Bytes(const char* inStr, int mode, unsigned int& outStrLen);


/**
 * @fn
 * @brief ��orgBuf�������ֽڴ��������,��֤����Ϊ8��������
 * @param orgBuf
 * @param orgLen 
 * @param mode
 * @param outStr ��䴮ָ��
 * @param outStrLen ��䴮�ķ���ռ䳤��; ������ַ�������:Ϊ�����ַ����������ϵ�8����
 * @return 0:�ɹ�����0:ʧ��
 */
int Padding8BinaryNoNew(const char* orgBuf, unsigned int orgLen, int mode, char* outStr, unsigned int& outStrLen);


/**
 * @fn
 * @brief ��orgBuf�������ֽڴ��������,��֤����Ϊ8��������
 * @param orgBuf
 * @param orgLen
 * @param mode
 * @param outStrLen ������ַ�������
 * @return ������ַ�ָ��,���ⲿ�������ͷ��ڴ�
 */
char* Padding8Binary(const char* orgBuf, unsigned int orgLen, int mode, unsigned int& outStrLen);


/**
 * @struct SBlock
 * @brief Block Structure
 */
struct SBlock
{
	//Constructors
	SBlock(unsigned int l=0, unsigned int r=0) : m_uil(l), m_uir(r) {}
	//Copy Constructor
	SBlock(const SBlock& roBlock) : m_uil(roBlock.m_uil), m_uir(roBlock.m_uir) {}
    
	SBlock& operator^=(SBlock& b) 
    {
        m_uil ^= b.m_uil; 
        m_uir ^= b.m_uir; 
        return *this; 
    }

    unsigned int m_uil;
    unsigned int m_uir;
};


/**
 * @class CBlowFish
 * @brief 
 */
class CBlowFish
{
public:
    /**
     * @enum
     * @brief
     */
    enum 
    { 
        ECB=0
        , CBC=1
        , CFB=2
    };

public:
	/**
	 * @fn
	 * @brief Constructor - Initialize the P and S boxes for a given Key
	 * @param
	 * @param
	 * @param
	 * @return 
	 */
	CBlowFish(unsigned char* ucKey, size_t n, const SBlock& roChain = SBlock(0UL,0UL));
    virtual ~CBlowFish();

	//Resetting the chaining block
	//void ResetChain() { m_oChain = m_oChain0; }
	
    /**
     * @fn
     * @brief Encrypt Buffer in Place
     * @param
     * @param
     * @param
     * @return Returns false if n is multiple of 8
     */
	int Encrypt(unsigned char* buf, size_t n, int iMode=ECB);

    /**
     * @fn
     * @brief Decrypt Buffer in Place
     * @param
     * @param
     * @param
     * @return Returns false if n is multiple of 8
     */
	int Decrypt(unsigned char* buf, size_t n, int iMode=ECB);

    /**
     * @fn
     * @brief Encrypt from Input Buffer to Output Buffer
     * @param 
     * @param 
     * @param
     * @param
     * @return Returns false if n is multiple of 8
     */
	int Encrypt(const unsigned char* in, unsigned char* out, size_t n, int iMode=ECB);

    /**
     * @fn
     * @brief Decrypt from Input Buffer to Output Buffer
     * @param
     * @param
     * @param 
     * @return Returns false if n is multiple of 8
     */
	int Decrypt(const unsigned char* in, unsigned char* out, size_t n, int iMode=ECB);

private:
	unsigned int BlowFishF(unsigned int ui);
    // Sixteen Round Encipher of Block
	void Encrypt(SBlock&);
    // Sixteen Round Decipher of SBlock
	void Decrypt(SBlock&);

private:
	//The Initialization Vector, by default {0, 0}
	SBlock m_oChain0;
	SBlock m_oChain;

    unsigned int m_auiP[18];
#if defined(__SYMBIAN32__)
    unsigned int* m_auiS[4];
#else
    unsigned int m_auiS[4][256];
	static const unsigned int scm_auiInitP[18];
	static const unsigned int scm_auiInitS[4][256];
#endif

};

//���ܺ���
extern "C"  int BlowFishEncryptBin(const char* orgBuf, int orgLen, char* outStr, int& outLen);

//���ܺ���
extern "C"  int BlowFishDecryptBin(const char* orgBuf, int orgLen, char* outStr, int& outLen);
 
#endif  /* !__BLOWFISH_H__ */
