/**
 * Dosya boyutunu formatlar
 * @param bytes - Byte cinsinden dosya boyutu
 * @returns Formatlanmış dosya boyutu string'i
 */
export const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

/**
 * Dosya türünü kontrol eder
 * @param fileName - Dosya adı
 * @param allowedExtensions - İzin verilen uzantılar
 * @returns Dosya türü geçerli mi
 */
export const isValidFileType = (fileName: string, allowedExtensions: string[]): boolean => {
    const extension = fileName.split('.').pop()?.toLowerCase();
    return extension ? allowedExtensions.includes(extension) : false;
};

/**
 * Dosya türüne göre CSS class döndürür
 * @param fileName - Dosya adı
 * @returns CSS class string
 */
export const getFileIconClass = (fileName: string): string => {
    const extension = fileName.split('.').pop()?.toLowerCase();

    switch (extension) {
        case 'pdf':
            return 'text-red-600';
        case 'doc':
        case 'docx':
            return 'text-blue-600';
        case 'jpg':
        case 'jpeg':
        case 'png':
        case 'gif':
        case 'webp':
            return 'text-green-600';
        case 'zip':
        case 'rar':
        case '7z':
            return 'text-yellow-600';
        default:
            return 'text-gray-600';
    }
};

/**
 * Dosya türünün resim olup olmadığını kontrol eder
 * @param fileName - Dosya adı
 * @returns Resim dosyası mı
 */
export const isImageFile = (fileName: string): boolean => {
    const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'];
    const extension = fileName.split('.').pop()?.toLowerCase();
    return extension ? imageExtensions.includes(extension) : false;
};

/**
 * Dosya türünün video olup olmadığını kontrol eder
 * @param fileName - Dosya adı
 * @returns Video dosyası mı
 */
export const isVideoFile = (fileName: string): boolean => {
    const videoExtensions = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm', 'mkv'];
    const extension = fileName.split('.').pop()?.toLowerCase();
    return extension ? videoExtensions.includes(extension) : false;
};

/**
 * Dosya türünün ses olup olmadığını kontrol eder
 * @param fileName - Dosya adı
 * @returns Ses dosyası mı
 */
export const isAudioFile = (fileName: string): boolean => {
    const audioExtensions = ['mp3', 'wav', 'ogg', 'aac', 'flac', 'wma'];
    const extension = fileName.split('.').pop()?.toLowerCase();
    return extension ? audioExtensions.includes(extension) : false;
};

/**
 * Dosya uzantısını döndürür
 * @param fileName - Dosya adı
 * @returns Dosya uzantısı
 */
export const getFileExtension = (fileName: string): string => {
    return fileName.split('.').pop()?.toLowerCase() || '';
};

/**
 * Dosya adını temizler (özel karakterleri kaldırır)
 * @param fileName - Dosya adı
 * @returns Temizlenmiş dosya adı
 */
export const sanitizeFileName = (fileName: string): string => {
    return fileName.replace(/[^a-zA-Z0-9.-_]/g, '_');
};