import React, { useState, useEffect } from 'react';
import { Users, Search, Plus, Edit, Trash2, RefreshCw, Eye } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { useAuthStore } from '../store/authStore';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Modal } from '../components/ui/Modal';
import { Card } from '../components/ui/Card';
import { Select } from '../components/ui/Select';

interface Customer {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    phone?: string;
    address?: string;
    companyName?: string;
    companyId: number;
    status: 'ACTIVE' | 'INACTIVE' | 'BLOCKED';
    ticketCount: number;
    lastTicketDate?: string;
    createdAt: string;
    updatedAt: string;
}

interface Company {
    id: number;
    name: string;
}

interface CreateCustomerForm {
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    address: string;
    companyId: number;
}

const Customers: React.FC = () => {
    const { user } = useAuthStore();
    const [customers, setCustomers] = useState<Customer[]>([]);
    const [companies, setCompanies] = useState<Company[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [total, setTotal] = useState(0);
    
    // Modal states
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showViewModal, setShowViewModal] = useState(false);
    const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
    
    // Form state
    const [formData, setFormData] = useState<CreateCustomerForm>({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        address: '',
        companyId: 0
    });

    // Check if user has permission to manage customers
    const canManageCustomers = ['SUPER_ADMIN', 'MANAGER', 'AGENT'].includes(user?.role || '');
    const canEditCustomers = ['SUPER_ADMIN', 'MANAGER'].includes(user?.role || '');

    const loadCustomers = async () => {
        try {
            setLoading(true);
            const params = new URLSearchParams({
                page: (page - 1).toString(),
                size: '10',
                ...(searchTerm && { search: searchTerm })
            });

            const response = await fetch(`http://localhost:8067/api/v1/customers?${params}`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Müşteriler yüklenirken hata oluştu');
            }

            const data = await response.json();
            console.log('Customers response:', data);

            if (data.customers) {
                // Debug customer data
                data.customers.forEach((customer: any, index: number) => {
                    console.log(`Customer ${index}:`, customer);
                    console.log(`Customer ${index} companyId:`, customer.companyId, typeof customer.companyId);
                    console.log(`Customer ${index} companyName:`, customer.companyName);
                });
                
                setCustomers(data.customers);
                setTotal(data.total || data.customers.length);
                setTotalPages(Math.ceil((data.total || data.customers.length) / 10));
            }
        } catch (error) {
            console.error('Error loading customers:', error);
            toast.error('Müşteriler yüklenirken hata oluştu');
        } finally {
            setLoading(false);
        }
    };

    const loadCompanies = async () => {
        try {
            const response = await fetch('http://localhost:8067/api/v1/companies', {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                    'Content-Type': 'application/json',
                },
            });

            if (response.ok) {
                const data = await response.json();
                if (data.companies) {
                    setCompanies(data.companies);
                }
            }
        } catch (error) {
            console.error('Error loading companies:', error);
        }
    };

    const handleCreateCustomer = async () => {
        try {
            const response = await fetch('http://localhost:8067/api/v1/customers', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Müşteri oluşturulurken hata oluştu');
            }

            toast.success('Müşteri başarıyla oluşturuldu');
            setShowCreateModal(false);
            resetForm();
            loadCustomers();
        } catch (error) {
            console.error('Error creating customer:', error);
            toast.error(error instanceof Error ? error.message : 'Müşteri oluşturulurken hata oluştu');
        }
    };

    const handleUpdateCustomer = async () => {
        if (!selectedCustomer) return;

        try {
            const response = await fetch(`http://localhost:8067/api/v1/customers/${selectedCustomer.id}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Müşteri güncellenirken hata oluştu');
            }

            toast.success('Müşteri başarıyla güncellendi');
            setShowEditModal(false);
            setSelectedCustomer(null);
            resetForm();
            loadCustomers();
        } catch (error) {
            console.error('Error updating customer:', error);
            toast.error(error instanceof Error ? error.message : 'Müşteri güncellenirken hata oluştu');
        }
    };

    const handleDeleteCustomer = async () => {
        if (!selectedCustomer) return;

        try {
            const response = await fetch(`http://localhost:8067/api/v1/customers/${selectedCustomer.id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                },
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Müşteri silinirken hata oluştu');
            }

            toast.success('Müşteri başarıyla silindi');
            setShowDeleteModal(false);
            setSelectedCustomer(null);
            loadCustomers();
        } catch (error) {
            console.error('Error deleting customer:', error);
            toast.error(error instanceof Error ? error.message : 'Müşteri silinirken hata oluştu');
        }
    };

    const openEditModal = (customer: Customer) => {
        console.log('🔧 Opening edit modal for customer:', customer);
        console.log('🏢 Customer companyId:', customer.companyId);
        
        setSelectedCustomer(customer);
        setFormData({
            firstName: customer.firstName,
            lastName: customer.lastName,
            email: customer.email,
            phone: customer.phone || '',
            address: customer.address || '',
            companyId: customer.companyId || 0
        });
        setShowEditModal(true);
    };

    const openDeleteModal = (customer: Customer) => {
        setSelectedCustomer(customer);
        setShowDeleteModal(true);
    };

    const openViewModal = (customer: Customer) => {
        setSelectedCustomer(customer);
        setShowViewModal(true);
    };

    const resetForm = () => {
        setFormData({
            firstName: '',
            lastName: '',
            email: '',
            phone: '',
            address: '',
            companyId: 0
        });
    };

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'ACTIVE': return 'text-success-600 bg-success-100';
            case 'INACTIVE': return 'text-gray-600 bg-gray-100';
            case 'BLOCKED': return 'text-danger-600 bg-danger-100';
            default: return 'text-gray-600 bg-gray-100';
        }
    };

    const getStatusText = (status: string) => {
        switch (status) {
            case 'ACTIVE': return 'Aktif';
            case 'INACTIVE': return 'Pasif';
            case 'BLOCKED': return 'Engellenmiş';
            default: return status;
        }
    };

    useEffect(() => {
        if (canManageCustomers) {
            loadCustomers();
            loadCompanies();
        }
    }, [page, searchTerm, canManageCustomers]);

    if (!canManageCustomers) {
        return (
            <div className="p-6">
                <Card className="p-6 text-center">
                    <div className="text-danger-600 mb-4">
                        <Users className="w-12 h-12 mx-auto" />
                    </div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-2">Erişim Yetkisi Yok</h3>
                    <p className="text-gray-600">
                        Bu sayfaya erişim yetkiniz bulunmamaktadır.
                    </p>
                </Card>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <Users className="w-8 h-8 text-primary-600" />
                    <div>
                        <h1 className="text-title-1 text-gray-900 font-bold">
                            Müşteri Yönetimi
                        </h1>
                        <p className="text-body text-gray-600 mt-1">
                            Müşteri profillerini yönetin ve takip edin
                        </p>
                    </div>
                </div>
                <div className="flex gap-2">
                    <Button
                        variant="ghost"
                        onClick={loadCustomers}
                        leftIcon={<RefreshCw className="w-4 h-4" />}
                    >
                        Yenile
                    </Button>
                    {canEditCustomers && (
                        <Button
                            variant="primary"
                            onClick={() => setShowCreateModal(true)}
                            leftIcon={<Plus className="w-4 h-4" />}
                        >
                            Yeni Müşteri
                        </Button>
                    )}
                </div>
            </div>

            {/* Search and Stats */}
            <div className="flex items-center justify-between">
                <div className="w-96">
                    <Input
                        placeholder="Müşteri ara..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        leftIcon={<Search className="w-4 h-4" />}
                    />
                </div>
                <div className="text-sm text-gray-500">
                    Toplam {total} müşteri
                </div>
            </div>

            {/* Customers Table */}
            <Card>
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Müşteri
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    İletişim
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Şirket
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Durum
                                </th>
                                <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Ticket
                                </th>
                                <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    İşlemler
                                </th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {loading ? (
                                <tr>
                                    <td colSpan={6} className="px-6 py-12 text-center">
                                        <div className="flex items-center justify-center">
                                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
                                            <span className="ml-2 text-gray-500">Yükleniyor...</span>
                                        </div>
                                    </td>
                                </tr>
                            ) : customers.length === 0 ? (
                                <tr>
                                    <td colSpan={6} className="px-6 py-12 text-center">
                                        <Users className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                                        <p className="text-gray-500">Müşteri bulunamadı</p>
                                    </td>
                                </tr>
                            ) : (
                                customers.map((customer) => (
                                    <tr key={customer.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4">
                                            <div>
                                                <div className="text-sm font-medium text-gray-900">
                                                    {customer.firstName} {customer.lastName}
                                                </div>
                                                {customer.address && (
                                                    <div className="text-sm text-gray-500">
                                                        {customer.address}
                                                    </div>
                                                )}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="text-sm text-gray-900">
                                                {customer.email}
                                            </div>
                                            <div className="text-sm text-gray-500">
                                                {customer.phone || '-'}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-sm text-gray-900">
                                            {customer.companyName || '-'}
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(customer.status)}`}>
                                                {getStatusText(customer.status)}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-center text-sm text-gray-900">
                                            {customer.ticketCount || 0}
                                        </td>
                                        <td className="px-6 py-4 text-center">
                                            <div className="flex items-center justify-center gap-2">
                                                <button
                                                    onClick={() => openViewModal(customer)}
                                                    className="text-info-600 hover:text-info-900"
                                                >
                                                    <Eye className="w-4 h-4" />
                                                </button>
                                                {canEditCustomers && (
                                                    <>
                                                        <button
                                                            onClick={() => openEditModal(customer)}
                                                            className="text-primary-600 hover:text-primary-900"
                                                        >
                                                            <Edit className="w-4 h-4" />
                                                        </button>
                                                        <button
                                                            onClick={() => openDeleteModal(customer)}
                                                            className="text-danger-600 hover:text-danger-900"
                                                        >
                                                            <Trash2 className="w-4 h-4" />
                                                        </button>
                                                    </>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Pagination */}
                {totalPages > 1 && (
                    <div className="px-6 py-4 border-t border-gray-200">
                        <div className="flex items-center justify-between">
                            <Button
                                variant="outline"
                                disabled={page <= 1}
                                onClick={() => setPage(page - 1)}
                            >
                                Önceki
                            </Button>
                            <span className="text-sm text-gray-700">
                                Sayfa {page} / {totalPages}
                            </span>
                            <Button
                                variant="outline"
                                disabled={page >= totalPages}
                                onClick={() => setPage(page + 1)}
                            >
                                Sonraki
                            </Button>
                        </div>
                    </div>
                )}
            </Card>

            {/* Create Customer Modal */}
            <Modal
                isOpen={showCreateModal}
                onClose={() => {setShowCreateModal(false); resetForm();}}
                title="Yeni Müşteri Ekle"
                size="lg"
            >
                <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                        <Input
                            label="Ad"
                            value={formData.firstName}
                            onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                            required
                        />
                        <Input
                            label="Soyad"
                            value={formData.lastName}
                            onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                            required
                        />
                    </div>
                    <Input
                        label="E-posta"
                        type="email"
                        value={formData.email}
                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        required
                    />
                    <Input
                        label="Telefon"
                        value={formData.phone}
                        onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    />
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Adres</label>
                        <textarea
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                            rows={2}
                            value={formData.address}
                            onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Şirket *</label>
                        <Select
                            value={formData.companyId ? formData.companyId.toString() : ''}
                            onChange={(e) => setFormData({ ...formData, companyId: Number(e.target.value) })}
                            placeholder="Şirket seçin"
                        >
                            {companies.map((company) => (
                                <option key={company.id} value={company.id}>
                                    {company.name}
                                </option>
                            ))}
                        </Select>
                    </div>
                </div>
                <div className="flex justify-end gap-3 mt-6">
                    <Button
                        variant="outline"
                        onClick={() => {setShowCreateModal(false); resetForm();}}
                    >
                        İptal
                    </Button>
                    <Button
                        variant="primary"
                        onClick={handleCreateCustomer}
                        disabled={!formData.firstName.trim() || !formData.lastName.trim() || !formData.email.trim() || !formData.companyId}
                    >
                        Oluştur
                    </Button>
                </div>
            </Modal>

            {/* Edit Customer Modal */}
            <Modal
                isOpen={showEditModal}
                onClose={() => {setShowEditModal(false); setSelectedCustomer(null); resetForm();}}
                title="Müşteri Düzenle"
                size="lg"
            >
                <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                        <Input
                            label="Ad"
                            value={formData.firstName}
                            onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                            required
                        />
                        <Input
                            label="Soyad"
                            value={formData.lastName}
                            onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                            required
                        />
                    </div>
                    <Input
                        label="E-posta"
                        type="email"
                        value={formData.email}
                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        required
                    />
                    <Input
                        label="Telefon"
                        value={formData.phone}
                        onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    />
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Adres</label>
                        <textarea
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                            rows={2}
                            value={formData.address}
                            onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Şirket *</label>
                        <Select
                            value={formData.companyId ? formData.companyId.toString() : ''}
                            onChange={(e) => setFormData({ ...formData, companyId: Number(e.target.value) })}
                        >
                            {companies.map((company) => (
                                <option key={company.id} value={company.id}>
                                    {company.name}
                                </option>
                            ))}
                        </Select>
                    </div>
                </div>
                <div className="flex justify-end gap-3 mt-6">
                    <Button
                        variant="outline"
                        onClick={() => {setShowEditModal(false); setSelectedCustomer(null); resetForm();}}
                    >
                        İptal
                    </Button>
                    <Button
                        variant="primary"
                        onClick={handleUpdateCustomer}
                        disabled={!formData.firstName.trim() || !formData.lastName.trim() || !formData.email.trim() || !formData.companyId}
                    >
                        Güncelle
                    </Button>
                </div>
            </Modal>

            {/* View Customer Modal */}
            <Modal
                isOpen={showViewModal}
                onClose={() => {setShowViewModal(false); setSelectedCustomer(null);}}
                title="Müşteri Detayları"
                size="lg"
            >
                {selectedCustomer && (
                    <div className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Ad</label>
                                <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-md">
                                    {selectedCustomer.firstName}
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Soyad</label>
                                <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-md">
                                    {selectedCustomer.lastName}
                                </div>
                            </div>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">E-posta</label>
                            <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-md">
                                {selectedCustomer.email}
                            </div>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Telefon</label>
                            <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-md">
                                {selectedCustomer.phone || '-'}
                            </div>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Adres</label>
                            <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-md min-h-[60px]">
                                {selectedCustomer.address || '-'}
                            </div>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Şirket</label>
                            <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-md">
                                {selectedCustomer.companyName || '-'}
                            </div>
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Durum</label>
                                <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-md">
                                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(selectedCustomer.status)}`}>
                                        {getStatusText(selectedCustomer.status)}
                                    </span>
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Ticket Sayısı</label>
                                <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-md">
                                    {selectedCustomer.ticketCount || 0}
                                </div>
                            </div>
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Kayıt Tarihi</label>
                                <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-md">
                                    {new Date(selectedCustomer.createdAt).toLocaleDateString('tr-TR')}
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Son Güncelleme</label>
                                <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-md">
                                    {new Date(selectedCustomer.updatedAt).toLocaleDateString('tr-TR')}
                                </div>
                            </div>
                        </div>
                    </div>
                )}
                <div className="flex justify-end mt-6">
                    <Button
                        variant="outline"
                        onClick={() => {setShowViewModal(false); setSelectedCustomer(null);}}
                    >
                        Kapat
                    </Button>
                </div>
            </Modal>

            {/* Delete Customer Modal */}
            <Modal
                isOpen={showDeleteModal}
                onClose={() => {setShowDeleteModal(false); setSelectedCustomer(null);}}
                title="Müşteri Sil"
                size="md"
            >
                <div className="text-center">
                    <div className="text-danger-600 mb-4">
                        <Users className="w-12 h-12 mx-auto" />
                    </div>
                    <p className="text-gray-600 mb-6">
                        <strong>{selectedCustomer?.firstName} {selectedCustomer?.lastName}</strong> müşterisini silmek istediğinizden emin misiniz?
                        <br />
                        Bu işlem geri alınamaz.
                    </p>
                    <div className="flex justify-center gap-3">
                        <Button
                            variant="outline"
                            onClick={() => {setShowDeleteModal(false); setSelectedCustomer(null);}}
                        >
                            İptal
                        </Button>
                        <Button
                            variant="danger"
                            onClick={handleDeleteCustomer}
                        >
                            Sil
                        </Button>
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default Customers;