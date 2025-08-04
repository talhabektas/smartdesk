import React from 'react';
import { Modal } from './Modal';
import { Button } from './Button';
import { AlertTriangle, Trash2, Users, UserCheck, UserX } from 'lucide-react';

export interface ConfirmationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  confirmVariant?: 'primary' | 'secondary' | 'danger' | 'success' | 'warning' | 'ghost' | 'outline' | 'info';
  isLoading?: boolean;
  type?: 'warning' | 'danger' | 'info' | 'success';
  icon?: React.ReactNode;
}

const ConfirmationModal: React.FC<ConfirmationModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  confirmVariant = 'primary',
  isLoading = false,
  type = 'warning',
  icon
}) => {
  const typeIcons = {
    warning: <AlertTriangle className="w-6 h-6 text-warning-500" />,
    danger: <AlertTriangle className="w-6 h-6 text-danger-500" />,
    info: <Users className="w-6 h-6 text-info-500" />,
    success: <UserCheck className="w-6 h-6 text-success-500" />
  };

  const typeColors = {
    warning: 'bg-warning-50 border-warning-200',
    danger: 'bg-danger-50 border-danger-200',
    info: 'bg-info-50 border-info-200',
    success: 'bg-success-50 border-success-200'
  };

  const displayIcon = icon || typeIcons[type];

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      size="md"
      closeOnOverlayClick={!isLoading}
    >
      <div className="text-center">
        {/* Icon */}
        <div className={`mx-auto flex items-center justify-center w-12 h-12 rounded-full border-2 mb-4 ${typeColors[type]}`}>
          {displayIcon}
        </div>

        {/* Title */}
        <h3 className="text-lg font-semibold text-gray-900 mb-2">
          {title}
        </h3>

        {/* Message */}
        <p className="text-sm text-gray-600 mb-6">
          {message}
        </p>

        {/* Actions */}
        <div className="flex items-center justify-center gap-3">
          <Button
            variant="secondary"
            onClick={onClose}
            disabled={isLoading}
          >
            {cancelText}
          </Button>
          <Button
            variant={confirmVariant}
            onClick={onConfirm}
            isLoading={isLoading}
          >
            {confirmText}
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export { ConfirmationModal };
export default ConfirmationModal;