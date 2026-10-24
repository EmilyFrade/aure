export function resizeImage(file: File, maxSize = 512, quality = 0.85): Promise<Blob> {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const image = new Image();
    image.onload = () => {
      const scale = Math.min(1, maxSize / Math.max(image.width, image.height));
      const canvas = document.createElement('canvas');
      canvas.width = Math.round(image.width * scale);
      canvas.height = Math.round(image.height * scale);
      canvas.getContext('2d')!.drawImage(image, 0, 0, canvas.width, canvas.height);
      URL.revokeObjectURL(url);
      canvas.toBlob((blob) => (blob ? resolve(blob) : reject(new Error('resize'))), 'image/jpeg', quality);
    };
    image.onerror = () => {
      URL.revokeObjectURL(url);
      reject(new Error('load'));
    };
    image.src = url;
  });
}
