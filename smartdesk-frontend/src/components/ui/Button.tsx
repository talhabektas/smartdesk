import React from 'react';
import { clsx } from 'clsx';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'success' | 'warning' | 'ghost' | 'outline' | 'info';
  size?: 'sm' | 'md' | 'lg' | 'xl';
  isLoading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  fullWidth?: boolean;
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({
    className,
    variant = 'primary',
    size = 'md',
    isLoading = false,
    leftIcon,
    rightIcon,
    fullWidth = false,
    children,
    disabled,
    ...props
  }, ref) => {
    const baseStyles = [
      'inline-flex',
      'items-center',
      'justify-center',
      'font-medium',
      'rounded-lg',
      'border',
      'transition-all',
      'duration-200',
      'ease-apple',
      'focus:outline-none',
      'focus:ring-2',
      'focus:ring-offset-2',
      'disabled:opacity-50',
      'disabled:cursor-not-allowed',
      'disabled:transform-none',
      'relative',
      'overflow-hidden',
      'apple-button',
    ];

    const variants = {
      primary: [
        'bg-gradient-to-r',
        'from-primary-500',
        'to-primary-600',
        'text-white',
        'border-primary-600',
        'shadow-primary',
        'hover:shadow-primary-hover',
        'hover:-translate-y-0.5',
        'focus:ring-primary-300',
        'active:scale-95',
      ],
      secondary: [
        'bg-white',
        'text-gray-700',
        'border-gray-200',
        'shadow-apple-sm',
        'hover:bg-gray-50',
        'hover:shadow-apple-md',
        'hover:-translate-y-0.5',
        'focus:ring-gray-300',
        'active:scale-95',
      ],
      danger: [
        'bg-gradient-to-r',
        'from-danger-500',
        'to-danger-600',
        'text-white',
        'border-danger-600',
        'shadow-danger',
        'hover:shadow-danger-hover',
        'hover:-translate-y-0.5',
        'focus:ring-danger-300',
        'active:scale-95',
      ],
      success: [
        'bg-gradient-to-r',
        'from-success-500',
        'to-success-600',
        'text-white',
        'border-success-600',
        'shadow-success',
        'hover:shadow-success-hover',
        'hover:-translate-y-0.5',
        'focus:ring-success-300',
        'active:scale-95',
      ],
      warning: [
        'bg-gradient-to-r',
        'from-warning-500',
        'to-warning-600',
        'text-white',
        'border-warning-600',
        'shadow-warning',
        'hover:shadow-warning-hover',
        'hover:-translate-y-0.5',
        'focus:ring-warning-300',
        'active:scale-95',
      ],
      info: [
        'bg-gradient-to-r',
        'from-info-500',
        'to-info-600',
        'text-white',
        'border-info-600',
        'shadow-info',
        'hover:shadow-info-hover',
        'hover:-translate-y-0.5',
        'focus:ring-info-300',
        'active:scale-95',
      ],
      ghost: [
        'bg-transparent',
        'text-gray-700',
        'border-transparent',
        'hover:bg-gray-100',
        'hover:shadow-apple-sm',
        'focus:ring-gray-300',
        'active:scale-95',
      ],
      outline: [
        'bg-transparent',
        'text-gray-700',
        'border-gray-300',
        'hover:bg-gray-50',
        'hover:border-gray-400',
        'hover:shadow-apple-sm',
        'focus:ring-gray-300',
        'active:scale-95',
      ],
    };

    const sizes = {
      sm: ['px-3', 'py-1.5', 'text-sm', 'gap-1.5'],
      md: ['px-4', 'py-2.5', 'text-sm', 'gap-2'],
      lg: ['px-6', 'py-3', 'text-base', 'gap-2'],
      xl: ['px-8', 'py-4', 'text-lg', 'gap-3'],
    };

    const widthClass = fullWidth ? 'w-full' : '';

    return (
      <button
        ref={ref}
        className={clsx(
          baseStyles,
          variants[variant],
          sizes[size],
          widthClass,
          className
        )}
        disabled={disabled || isLoading}
        {...props}
      >
        {/* Shimmer effect overlay */}
        <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent opacity-0 hover:opacity-100 transition-opacity duration-300 transform -skew-x-12 -translate-x-full hover:translate-x-full" />
        
        {isLoading && (
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-apple-spin opacity-75" />
          </div>
        )}
        
        <div className={clsx(
          'flex items-center justify-center gap-2 transition-opacity duration-200',
          isLoading && 'opacity-0'
        )}>
          {leftIcon && (
            <span className="flex-shrink-0 transition-transform duration-200 group-hover:scale-110">
              {leftIcon}
            </span>
          )}
          {children && (
            <span className="font-medium leading-none">
              {children}
            </span>
          )}
          {rightIcon && (
            <span className="flex-shrink-0 transition-transform duration-200 group-hover:scale-110">
              {rightIcon}
            </span>
          )}
        </div>
      </button>
    );
  }
);

Button.displayName = 'Button';

export { Button };
export default Button;