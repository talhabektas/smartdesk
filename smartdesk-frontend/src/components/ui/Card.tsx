import React from 'react';
import { clsx } from 'clsx';

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  className?: string;
  variant?: 'default' | 'elevated' | 'glass' | 'bordered';
  size?: 'sm' | 'md' | 'lg' | 'xl';
  interactive?: boolean;
  onClick?: () => void;
}

const Card: React.FC<CardProps> & {
  Header: typeof CardHeader;
  Content: typeof CardContent;
  Footer: typeof CardFooter;
} = ({
  children,
  className,
  variant = 'default',
  size = 'md',
  interactive = false,
  onClick,
  ...props
}) => {
  const baseStyles = [
    'rounded-xl',
    'transition-all',
    'duration-200',
    'ease-apple',
  ];

  const variants = {
    default: [
      'bg-white',
      'border',
      'border-gray-100',
      'shadow-apple-sm',
    ],
    elevated: [
      'bg-white',
      'shadow-apple-lg',
      'border-0',
    ],
    glass: [
      'glass',
      'border-white/20',
    ],
    bordered: [
      'bg-white',
      'border-2',
      'border-gray-200',
      'shadow-none',
    ],
  };

  const sizes = {
    sm: 'p-4',
    md: 'p-6',
    lg: 'p-8',
    xl: 'p-10',
  };

  const interactiveStyles = interactive || onClick ? [
    'cursor-pointer',
    'hover:shadow-apple-md',
    'hover:-translate-y-1',
    'active:scale-[0.98]',
    'hover:border-gray-200',
  ] : [];

  if (onClick) {
    return (
      <button
        className={clsx(
          baseStyles,
          variants[variant],
          sizes[size],
          interactiveStyles,
          className
        )}
        onClick={onClick}
        type="button"
        {...(props as React.ButtonHTMLAttributes<HTMLButtonElement>)}
      >
        {children}
      </button>
    );
  }

  return (
    <div
      className={clsx(
        baseStyles,
        variants[variant],
        sizes[size],
        interactiveStyles,
        className
      )}
      {...(props as React.HTMLAttributes<HTMLDivElement>)}
    >
      {children}
    </div>
  );
};

// Card Header Component
export interface CardHeaderProps {
  children?: React.ReactNode;
  className?: string;
  title?: string;
  subtitle?: string;
  actions?: React.ReactNode;
}

const CardHeader: React.FC<CardHeaderProps> = ({
  children,
  className,
  title,
  subtitle,
  actions,
}) => {
  return (
    <div className={clsx('flex items-start justify-between mb-6', className)}>
      <div className="flex-1">
        {title && (
          <h3 className="text-title-3 text-gray-900 font-semibold">
            {title}
          </h3>
        )}
        {subtitle && (
          <p className="text-caption text-gray-500 mt-1">
            {subtitle}
          </p>
        )}
        {children}
      </div>
      {actions && (
        <div className="ml-4 flex-shrink-0">
          {actions}
        </div>
      )}
    </div>
  );
};

// Card Content Component
export interface CardContentProps {
  children: React.ReactNode;
  className?: string;
}

const CardContent: React.FC<CardContentProps> = ({
  children,
  className,
}) => {
  return (
    <div className={clsx('text-body text-gray-700', className)}>
      {children}
    </div>
  );
};

// Card Footer Component
export interface CardFooterProps {
  children: React.ReactNode;
  className?: string;
  align?: 'left' | 'center' | 'right' | 'between';
}

const CardFooter: React.FC<CardFooterProps> = ({
  children,
  className,
  align = 'right',
}) => {
  const alignmentStyles = {
    left: 'justify-start',
    center: 'justify-center',
    right: 'justify-end',
    between: 'justify-between',
  };

  return (
    <div className={clsx(
      'flex items-center gap-3 mt-6 pt-4 border-t border-gray-100',
      alignmentStyles[align],
      className
    )}>
      {children}
    </div>
  );
};

// Stats Card Component
export interface StatsCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon?: React.ReactNode;
  trend?: {
    value: number;
    isPositive: boolean;
  };
  color?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
  className?: string;
}

const StatsCard: React.FC<StatsCardProps> = ({
  title,
  value,
  subtitle,
  icon,
  trend,
  color = 'primary',
  className,
}) => {
  const colorStyles = {
    primary: {
      bg: 'bg-primary-50',
      icon: 'text-primary-600',
      accent: 'border-primary-200',
    },
    success: {
      bg: 'bg-success-50',
      icon: 'text-success-600',
      accent: 'border-success-200',
    },
    warning: {
      bg: 'bg-warning-50',
      icon: 'text-warning-600',
      accent: 'border-warning-200',
    },
    danger: {
      bg: 'bg-danger-50',
      icon: 'text-danger-600',
      accent: 'border-danger-200',
    },
    info: {
      bg: 'bg-info-50',
      icon: 'text-info-600',
      accent: 'border-info-200',
    },
  };

  return (
    <Card className={clsx('relative overflow-hidden', className)} variant="elevated">
      {/* Background Pattern */}
      <div className="absolute inset-0 opacity-5">
        <div className="absolute -right-4 -top-4 w-24 h-24 rounded-full bg-current" />
        <div className="absolute -right-8 -top-8 w-16 h-16 rounded-full bg-current" />
      </div>
      
      <div className="relative">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <p className="text-caption text-gray-500 font-medium uppercase tracking-wider">
              {title}
            </p>
            <p className="text-title-1 text-gray-900 font-bold mt-2">
              {typeof value === 'number' ? value.toLocaleString() : value}
            </p>
            {subtitle && (
              <p className="text-caption text-gray-600 mt-1">
                {subtitle}
              </p>
            )}
            {trend && (
              <div className="flex items-center mt-2">
                <span className={clsx(
                  'text-sm font-medium',
                  trend.isPositive ? 'text-success-600' : 'text-danger-600'
                )}>
                  {trend.isPositive ? '+' : ''}{trend.value}%
                </span>
                <span className="text-caption text-gray-500 ml-2">
                  vs last period
                </span>
              </div>
            )}
          </div>
          {icon && (
            <div className={clsx(
              'flex-shrink-0 w-12 h-12 rounded-xl flex items-center justify-center',
              colorStyles[color].bg,
              colorStyles[color].icon
            )}>
              {icon}
            </div>
          )}
        </div>
      </div>
    </Card>
  );
};

// Feature Card Component
export interface FeatureCardProps {
  title: string;
  description: string;
  icon?: React.ReactNode;
  image?: string;
  action?: {
    label: string;
    onClick: () => void;
  };
  className?: string;
}

const FeatureCard: React.FC<FeatureCardProps> = ({
  title,
  description,
  icon,
  image,
  action,
  className,
}) => {
  return (
    <Card className={clsx('group', className)} interactive>
      {image && (
        <div className="aspect-video rounded-lg overflow-hidden mb-4 bg-gray-100">
          <img
            src={image}
            alt={title}
            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
          />
        </div>
      )}
      
      <div className="flex items-start gap-4">
        {icon && (
          <div className="flex-shrink-0 w-10 h-10 bg-primary-100 text-primary-600 rounded-lg flex items-center justify-center">
            {icon}
          </div>
        )}
        <div className="flex-1">
          <h3 className="text-headline text-gray-900 font-semibold mb-2">
            {title}
          </h3>
          <p className="text-body text-gray-600 mb-4">
            {description}
          </p>
          {action && (
            <button
              onClick={action.onClick}
              className="text-primary-600 hover:text-primary-700 font-medium text-sm transition-colors duration-200"
            >
              {action.label} →
            </button>
          )}
        </div>
      </div>
    </Card>
  );
};

// Attach sub-components to main Card component
Card.Header = CardHeader;
Card.Content = CardContent;
Card.Footer = CardFooter;

export { Card, CardHeader, CardContent, CardFooter, StatsCard, FeatureCard };
export default Card;