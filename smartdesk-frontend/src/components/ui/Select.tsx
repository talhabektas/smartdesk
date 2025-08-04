import React from 'react';

interface SelectProps {
    children: React.ReactNode;
    value?: string;
    onChange?: (e: React.ChangeEvent<HTMLSelectElement>) => void;
    className?: string;
    placeholder?: string;
    disabled?: boolean;
}

export const Select: React.FC<SelectProps> = ({
    children,
    value,
    onChange,
    className = '',
    placeholder,
    disabled = false
}) => {
    const baseClasses = 'w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500';
    const classes = `${baseClasses} ${className}`;

    return (
        <select
            value={value}
            onChange={onChange}
            className={classes}
            disabled={disabled}
        >
            {placeholder && (
                <option value="" disabled>
                    {placeholder}
                </option>
            )}
            {children}
        </select>
    );
}; 