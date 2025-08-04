import React from 'react';
import { clsx } from 'clsx';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  helperText?: string;
  isLoading?: boolean;
}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({
    className,
    type = 'text',
    label,
    error,
    leftIcon,
    rightIcon,
    helperText,
    isLoading = false,
    id,
    ...props
  }, ref) => {
    const inputId = id || `input-${Math.random().toString(36).substr(2, 9)}`;

    const baseStyles = [
      'block',
      'w-full',
      'rounded-md',
      'border',
      'shadow-sm',
      'transition-colors',
      'focus:outline-none',
      'focus:ring-2',
      'focus:ring-offset-2',
      'disabled:opacity-50',
      'disabled:cursor-not-allowed',
    ];

    const normalStyles = [
      'border-gray-300',
      'bg-white',
      'text-gray-900',
      'placeholder-gray-400',
      'focus:border-primary-500',
      'focus:ring-primary-500',
    ];

    const errorStyles = [
      'border-danger-300',
      'bg-white',
      'text-gray-900',
      'placeholder-gray-400',
      'focus:border-danger-500',
      'focus:ring-danger-500',
    ];

    const sizeStyles = leftIcon || rightIcon ? 'pl-10 pr-4 py-2' : 'px-3 py-2';

    return (
      <div className="w-full">
        {label && (
          <label
            htmlFor={inputId}
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            {label}
            {props.required && <span className="text-danger-500 ml-1">*</span>}
          </label>
        )}
        
        <div className="relative">
          {leftIcon && (
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <span className="text-gray-400 sm:text-sm">{leftIcon}</span>
            </div>
          )}
          
          <input
            ref={ref}
            id={inputId}
            type={type}
            className={clsx(
              baseStyles,
              error ? errorStyles : normalStyles,
              sizeStyles,
              className
            )}
            {...props}
          />
          
          {(rightIcon || isLoading) && (
            <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
              {isLoading ? (
                <svg className="animate-spin h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24">
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  />
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  />
                </svg>
              ) : (
                rightIcon && <span className="text-gray-400 sm:text-sm">{rightIcon}</span>
              )}
            </div>
          )}
        </div>
        
        {(error || helperText) && (
          <div className="mt-1">
            {error ? (
              <p className="text-sm text-danger-600">{error}</p>
            ) : (
              helperText && <p className="text-sm text-gray-500">{helperText}</p>
            )}
          </div>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

export { Input };
export default Input;