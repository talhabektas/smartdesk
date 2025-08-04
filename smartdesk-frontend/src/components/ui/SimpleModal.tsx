import React from 'react';
import { X } from 'lucide-react';
import { clsx } from 'clsx';

export interface SimpleModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: React.ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl' | '2xl';
  className?: string;
}

const SimpleModal: React.FC<SimpleModalProps> = ({
  isOpen,
  onClose,
  title,
  children,
  size = 'md',
  className,
}) => {
  console.log('🔵 SimpleModal render - isOpen:', isOpen, 'title:', title);
  
  if (!isOpen) {
    console.log('❌ SimpleModal not opening - isOpen is false');
    return null;
  }
  
  console.log('✅ SimpleModal opening!');

  const sizeClasses = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-xl',
    '2xl': 'max-w-2xl',
  };

  return (
    <div className="fixed inset-0 z-[99999] overflow-y-auto" style={{ pointerEvents: 'auto' }}>
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-black bg-opacity-25 backdrop-blur-sm"
        onClick={onClose}
        style={{ pointerEvents: 'auto' }}
      />
      
      {/* Modal */}
      <div className="flex min-h-full items-center justify-center p-4" style={{ position: 'relative', zIndex: 100000 }}>
        <div
          className={clsx(
            'relative w-full transform overflow-hidden rounded-2xl bg-white p-6 text-left align-middle shadow-xl transition-all',
            sizeClasses[size],
            className
          )}
          onClick={(e) => {
            e.stopPropagation();
            console.log('🖱️ Modal content clicked');
          }}
          style={{ pointerEvents: 'auto', position: 'relative', zIndex: 100001 }}
        >
          {/* Header */}
          {title && (
            <div className="flex items-start justify-between mb-4">
              <h3 className="text-lg font-medium leading-6 text-gray-900">
                {title}
              </h3>
              <button
                type="button"
                className="ml-4 inline-flex items-center justify-center rounded-md p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-primary-500"
                onClick={onClose}
              >
                <span className="sr-only">Kapat</span>
                <X className="h-5 w-5" aria-hidden="true" />
              </button>
            </div>
          )}

          {/* Content */}
          <div className="mt-4">
            {children}
          </div>
        </div>
      </div>
    </div>
  );
};

export { SimpleModal };
export default SimpleModal;