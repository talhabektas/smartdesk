import React, { useState, useEffect } from 'react';
import { Building2, Search, Plus, Edit, Trash2, RefreshCw, Users, Settings } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { useAuthStore } from '../store/authStore';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Modal } from '../components/ui/Modal';
import { SimpleModal } from '../components/ui/SimpleModal';
import { Card } from '../components/ui/Card';

interface Company {
    id: number;
    name: string;
    description?: string;
    address?: string;
    phone?: string;
    email?: string;
    website?: string;
    status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
    userCount: number;
    ticketCount: number;
    departmentCount?: number;
    createdAt: string;
    updatedAt: string;
}

interface Department {
    id: number;
    name: string;
    description?: string;
    companyId: number;
    userCount: number;
    ticketCount: number;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
}

interface CreateCompanyForm {
    name: string;
    description: string;
    address: string;
    phone: string;
    email: string;
    website: string;
}

interface CreateDepartmentForm {
    name: string;
    description: string;
}

const Companies: React.FC = () => {
    const { user } = useAuthStore();
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
    const [selectedCompany, setSelectedCompany] = useState<Company | null>(null);
    
    // Department management states
    const [showDepartmentsModal, setShowDepartmentsModal] = useState(false);
    const [showCreateDepartmentModal, setShowCreateDepartmentModal] = useState(false);
    const [showFirstDepartmentModal, setShowFirstDepartmentModal] = useState(false);
    const [showEditDepartmentModal, setShowEditDepartmentModal] = useState(false);
    const [showDeleteDepartmentModal, setShowDeleteDepartmentModal] = useState(false);
    const [selectedDepartment, setSelectedDepartment] = useState<Department | null>(null);
    const [departments, setDepartments] = useState<Department[]>([]);
    const [departmentsLoading, setDepartmentsLoading] = useState(false);
    
    // Form state
    const [formData, setFormData] = useState<CreateCompanyForm>({
        name: '',
        description: '',
        address: '',
        phone: '',
        email: '',
        website: ''
    });

    // Department form state
    const [departmentFormData, setDepartmentFormData] = useState<CreateDepartmentForm>({
        name: '',
        description: ''
    });

    // Debug department form data changes
    useEffect(() => {
        console.log('🔄 departmentFormData changed:', departmentFormData);
    }, [departmentFormData]);

    // Check if user has permission to manage companies
    const canManageCompanies = user?.role === 'SUPER_ADMIN';

    const loadCompanies = async () => {
        try {
            setLoading(true);
            const params = new URLSearchParams({
                page: (page - 1).toString(),
                size: '10',
                ...(searchTerm && { search: searchTerm })
            });

            const response = await fetch(`http://localhost:8067/api/v1/companies?${params}`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Companies yüklenirken hata oluştu');
            }

            const data = await response.json();
            console.log('Companies response:', data);

            if (data.companies) {
                setCompanies(data.companies);
                setTotal(data.total || data.companies.length);
                setTotalPages(Math.ceil((data.total || data.companies.length) / 10));
            }
        } catch (error) {
            console.error('Error loading companies:', error);
            toast.error('Şirketler yüklenirken hata oluştu');
        } finally {
            setLoading(false);
        }
    };

    const handleCreateCompany = async () => {
        try {
            const response = await fetch('http://localhost:8067/api/v1/companies', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Şirket oluşturulurken hata oluştu');
            }

            toast.success('Şirket başarıyla oluşturuldu');
            setShowCreateModal(false);
            resetForm();
            loadCompanies();
        } catch (error) {
            console.error('Error creating company:', error);
            toast.error(error instanceof Error ? error.message : 'Şirket oluşturulurken hata oluştu');
        }
    };

    const handleUpdateCompany = async () => {
        if (!selectedCompany) return;

        try {
            const response = await fetch(`http://localhost:8067/api/v1/companies/${selectedCompany.id}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Şirket güncellenirken hata oluştu');
            }

            toast.success('Şirket başarıyla güncellendi');
            setShowEditModal(false);
            setSelectedCompany(null);
            resetForm();
            loadCompanies();
        } catch (error) {
            console.error('Error updating company:', error);
            toast.error(error instanceof Error ? error.message : 'Şirket güncellenirken hata oluştu');
        }
    };

    const handleDeleteCompany = async () => {
        if (!selectedCompany) return;

        try {
            const response = await fetch(`http://localhost:8067/api/v1/companies/${selectedCompany.id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                },
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Şirket silinirken hata oluştu');
            }

            toast.success('Şirket başarıyla silindi');
            setShowDeleteModal(false);
            setSelectedCompany(null);
            loadCompanies();
        } catch (error) {
            console.error('Error deleting company:', error);
            toast.error(error instanceof Error ? error.message : 'Şirket silinirken hata oluştu');
        }
    };

    const openEditModal = (company: Company) => {
        setSelectedCompany(company);
        setFormData({
            name: company.name,
            description: company.description || '',
            address: company.address || '',
            phone: company.phone || '',
            email: company.email || '',
            website: company.website || ''
        });
        setShowEditModal(true);
    };

    const openDeleteModal = (company: Company) => {
        setSelectedCompany(company);
        setShowDeleteModal(true);
    };

    const loadDepartments = async (companyId: number) => {
        try {
            setDepartmentsLoading(true);
            const response = await fetch(`http://localhost:8067/api/v1/departments/company/${companyId}`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                    'Content-Type': 'application/json',
                },
            });

            if (response.ok) {
                const data = await response.json();
                setDepartments(data.departments || []);
            } else {
                throw new Error('Departmanlar yüklenirken hata oluştu');
            }
        } catch (error) {
            console.error('Error loading departments:', error);
            toast.error('Departmanlar yüklenirken hata oluştu');
        } finally {
            setDepartmentsLoading(false);
        }
    };

    const handleCreateDepartment = async () => {
        console.log('🔧 handleCreateDepartment called');
        console.log('🏢 selectedCompany:', selectedCompany);
        console.log('📝 departmentFormData:', departmentFormData);
        
        if (!selectedCompany) {
            console.error('❌ No selectedCompany');
            return;
        }

        const payload = {
            ...departmentFormData,
            companyId: selectedCompany.id
        };
        console.log('📤 Sending payload:', payload);

        try {
            const response = await fetch('http://localhost:8067/api/v1/departments', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
            });

            console.log('📥 Response status:', response.status);
            console.log('📥 Response ok:', response.ok);

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Departman oluşturulurken hata oluştu');
            }

            toast.success('Departman başarıyla oluşturuldu');
            setShowCreateDepartmentModal(false);
            setShowFirstDepartmentModal(false);
            resetDepartmentForm();
            
            // Departmanları yenile ve ana modalı tekrar aç
            await loadDepartments(selectedCompany.id);
            setTimeout(() => {
                setShowDepartmentsModal(true);
                console.log('✅ Ana departman modalı tekrar açıldı');
            }, 500);
        } catch (error) {
            console.error('Error creating department:', error);
            toast.error(error instanceof Error ? error.message : 'Departman oluşturulurken hata oluştu');
        }
    };

    const handleUpdateDepartment = async () => {
        if (!selectedDepartment || !selectedCompany) return;

        try {
            const response = await fetch(`http://localhost:8067/api/v1/departments/${selectedDepartment.id}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(departmentFormData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Departman güncellenirken hata oluştu');
            }

            toast.success('Departman başarıyla güncellendi');
            setShowEditDepartmentModal(false);
            setSelectedDepartment(null);
            resetDepartmentForm();
            loadDepartments(selectedCompany.id);
        } catch (error) {
            console.error('Error updating department:', error);
            toast.error(error instanceof Error ? error.message : 'Departman güncellenirken hata oluştu');
        }
    };

    const handleDeleteDepartment = async () => {
        if (!selectedDepartment || !selectedCompany) return;

        try {
            const response = await fetch(`http://localhost:8067/api/v1/departments/${selectedDepartment.id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
                },
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Departman silinirken hata oluştu');
            }

            toast.success('Departman başarıyla silindi');
            setShowDeleteDepartmentModal(false);
            setSelectedDepartment(null);
            loadDepartments(selectedCompany.id);
        } catch (error) {
            console.error('Error deleting department:', error);
            toast.error(error instanceof Error ? error.message : 'Departman silinirken hata oluştu');
        }
    };

    const openDepartmentsModal = (company: Company) => {
        console.log('🏢 openDepartmentsModal called with company:', company);
        setSelectedCompany(company);
        setShowDepartmentsModal(true);
        loadDepartments(company.id);
    };

    const openEditDepartmentModal = (department: Department) => {
        setSelectedDepartment(department);
        setDepartmentFormData({
            name: department.name,
            description: department.description || ''
        });
        setShowEditDepartmentModal(true);
    };

    const openDeleteDepartmentModal = (department: Department) => {
        setSelectedDepartment(department);
        setShowDeleteDepartmentModal(true);
    };

    const resetForm = () => {
        setFormData({
            name: '',
            description: '',
            address: '',
            phone: '',
            email: '',
            website: ''
        });
    };

    const resetDepartmentForm = () => {
        console.log('🔄 Resetting department form');
        setDepartmentFormData({
            name: '',
            description: ''
        });
    };

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'ACTIVE': return 'text-success-600 bg-success-100';
            case 'INACTIVE': return 'text-gray-600 bg-gray-100';
            case 'SUSPENDED': return 'text-danger-600 bg-danger-100';
            default: return 'text-gray-600 bg-gray-100';
        }
    };

    const getStatusText = (status: string) => {
        switch (status) {
            case 'ACTIVE': return 'Aktif';
            case 'INACTIVE': return 'Pasif';
            case 'SUSPENDED': return 'Askıda';
            default: return status;
        }
    };

    useEffect(() => {
        if (canManageCompanies) {
            loadCompanies();
        }
    }, [page, searchTerm, canManageCompanies]);

    // Debug selectedCompany changes
    useEffect(() => {
        console.log('🏢 selectedCompany changed:', selectedCompany);
    }, [selectedCompany]);

    if (!canManageCompanies) {
        return (
            <div className="p-6">
                <Card className="p-6 text-center">
                    <div className="text-danger-600 mb-4">
                        <Building2 className="w-12 h-12 mx-auto" />
                    </div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-2">Erişim Yetkisi Yok</h3>
                    <p className="text-gray-600">
                        Bu sayfaya erişim yetkiniz bulunmamaktadır. Sadece Süper Yöneticiler şirketleri yönetebilir.
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
                    <Building2 className="w-8 h-8 text-primary-600" />
                    <div>
                        <h1 className="text-title-1 text-gray-900 font-bold">
                            Şirket Yönetimi
                        </h1>
                        <p className="text-body text-gray-600 mt-1">
                            Sistem genelindeki şirketleri yönetin
                        </p>
                    </div>
                </div>
                <div className="flex gap-2">
                    <Button
                        variant="ghost"
                        onClick={loadCompanies}
                        leftIcon={<RefreshCw className="w-4 h-4" />}
                    >
                        Yenile
                    </Button>
                    <Button
                        variant="primary"
                        onClick={() => setShowCreateModal(true)}
                        leftIcon={<Plus className="w-4 h-4" />}
                    >
                        Yeni Şirket
                    </Button>
                </div>
            </div>

            {/* Search and Stats */}
            <div className="flex items-center justify-between">
                <div className="w-96">
                    <Input
                        placeholder="Şirket ara..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        leftIcon={<Search className="w-4 h-4" />}
                    />
                </div>
                <div className="text-sm text-gray-500">
                    Toplam {total} şirket
                </div>
            </div>

            {/* Companies Table */}
            <Card>
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Şirket
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    İletişim
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Durum
                                </th>
                                <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Kullanıcı
                                </th>
                                <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Ticket
                                </th>
                                <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Departman
                                </th>
                                <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    İşlemler
                                </th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {loading ? (
                                <tr>
                                    <td colSpan={7} className="px-6 py-12 text-center">
                                        <div className="flex items-center justify-center">
                                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
                                            <span className="ml-2 text-gray-500">Yükleniyor...</span>
                                        </div>
                                    </td>
                                </tr>
                            ) : companies.length === 0 ? (
                                <tr>
                                    <td colSpan={7} className="px-6 py-12 text-center">
                                        <Building2 className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                                        <p className="text-gray-500">Şirket bulunamadı</p>
                                    </td>
                                </tr>
                            ) : (
                                companies.map((company) => (
                                    <tr key={company.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4">
                                            <div>
                                                <div className="text-sm font-medium text-gray-900">
                                                    {company.name}
                                                </div>
                                                {company.description && (
                                                    <div className="text-sm text-gray-500">
                                                        {company.description}
                                                    </div>
                                                )}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="text-sm text-gray-900">
                                                {company.email || '-'}
                                            </div>
                                            <div className="text-sm text-gray-500">
                                                {company.phone || '-'}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(company.status)}`}>
                                                {getStatusText(company.status)}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-center text-sm text-gray-900">
                                            {company.userCount || 0}
                                        </td>
                                        <td className="px-6 py-4 text-center text-sm text-gray-900">
                                            {company.ticketCount || 0}
                                        </td>
                                        <td className="px-6 py-4 text-center">
                                            <button
                                                onClick={() => openDepartmentsModal(company)}
                                                className="inline-flex items-center gap-1 text-info-600 hover:text-info-900 text-sm"
                                            >
                                                <Users className="w-4 h-4" />
                                                {company.departmentCount || 0} Departman
                                            </button>
                                        </td>
                                        <td className="px-6 py-4 text-center">
                                            <div className="flex items-center justify-center gap-2">
                                                <button
                                                    onClick={() => openDepartmentsModal(company)}
                                                    className="text-info-600 hover:text-info-900"
                                                    title="Departmanları Yönet"
                                                >
                                                    <Settings className="w-4 h-4" />
                                                </button>
                                                <button
                                                    onClick={() => openEditModal(company)}
                                                    className="text-primary-600 hover:text-primary-900"
                                                >
                                                    <Edit className="w-4 h-4" />
                                                </button>
                                                <button
                                                    onClick={() => openDeleteModal(company)}
                                                    className="text-danger-600 hover:text-danger-900"
                                                >
                                                    <Trash2 className="w-4 h-4" />
                                                </button>
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

            {/* Create Company Modal */}
            <Modal
                isOpen={showCreateModal}
                onClose={() => {setShowCreateModal(false); resetForm();}}
                title="Yeni Şirket Ekle"
                size="lg"
            >
                <div className="space-y-4">
                    <Input
                        label="Şirket Adı"
                        value={formData.name}
                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                        required
                    />
                    <div className="grid grid-cols-2 gap-4">
                        <Input
                            label="E-posta"
                            type="email"
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        />
                        <Input
                            label="Telefon"
                            value={formData.phone}
                            onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                        />
                    </div>
                    <Input
                        label="Website"
                        value={formData.website}
                        onChange={(e) => setFormData({ ...formData, website: e.target.value })}
                    />
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Açıklama</label>
                        <textarea
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                            rows={3}
                            value={formData.description}
                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Adres</label>
                        <textarea
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                            rows={2}
                            value={formData.address}
                            onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                        />
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
                        onClick={handleCreateCompany}
                        disabled={!formData.name.trim()}
                    >
                        Oluştur
                    </Button>
                </div>
            </Modal>

            {/* Edit Company Modal */}
            <Modal
                isOpen={showEditModal}
                onClose={() => {setShowEditModal(false); setSelectedCompany(null); resetForm();}}
                title="Şirket Düzenle"
                size="lg"
            >
                <div className="space-y-4">
                    <Input
                        label="Şirket Adı"
                        value={formData.name}
                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                        required
                    />
                    <div className="grid grid-cols-2 gap-4">
                        <Input
                            label="E-posta"
                            type="email"
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        />
                        <Input
                            label="Telefon"
                            value={formData.phone}
                            onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                        />
                    </div>
                    <Input
                        label="Website"
                        value={formData.website}
                        onChange={(e) => setFormData({ ...formData, website: e.target.value })}
                    />
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Açıklama</label>
                        <textarea
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                            rows={3}
                            value={formData.description}
                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Adres</label>
                        <textarea
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                            rows={2}
                            value={formData.address}
                            onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                        />
                    </div>
                </div>
                <div className="flex justify-end gap-3 mt-6">
                    <Button
                        variant="outline"
                        onClick={() => {setShowEditModal(false); setSelectedCompany(null); resetForm();}}
                    >
                        İptal
                    </Button>
                    <Button
                        variant="primary"
                        onClick={handleUpdateCompany}
                        disabled={!formData.name.trim()}
                    >
                        Güncelle
                    </Button>
                </div>
            </Modal>

            {/* Delete Company Modal */}
            <Modal
                isOpen={showDeleteModal}
                onClose={() => {setShowDeleteModal(false); setSelectedCompany(null);}}
                title="Şirket Sil"
                size="md"
            >
                <div className="text-center">
                    <div className="text-danger-600 mb-4">
                        <Building2 className="w-12 h-12 mx-auto" />
                    </div>
                    <p className="text-gray-600 mb-6">
                        <strong>{selectedCompany?.name}</strong> şirketini silmek istediğinizden emin misiniz?
                        <br />
                        Bu işlem geri alınamaz.
                    </p>
                    <div className="flex justify-center gap-3">
                        <Button
                            variant="outline"
                            onClick={() => {setShowDeleteModal(false); setSelectedCompany(null);}}
                        >
                            İptal
                        </Button>
                        <Button
                            variant="danger"
                            onClick={handleDeleteCompany}
                        >
                            Sil
                        </Button>
                    </div>
                </div>
            </Modal>

            {/* Departments Management Modal */}
            <Modal
                isOpen={showDepartmentsModal}
                onClose={() => {
                    setShowDepartmentsModal(false); 
                    setSelectedCompany(null); 
                    setDepartments([]);
                    // Close all department sub-modals
                    setShowCreateDepartmentModal(false);
                    setShowEditDepartmentModal(false);
                    setShowDeleteDepartmentModal(false);
                    resetDepartmentForm();
                }}
                title={`Departman Yönetimi - ${selectedCompany?.name || 'Yükleniyor...'}`}
                size="4xl"
                closeOnOverlayClick={false}
            >
                <div className="space-y-4" onClick={(e) => e.stopPropagation()}>
                    {/* Header with Add Department Button */}
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <Users className="w-5 h-5 text-primary-600" />
                            <span className="text-lg font-medium">Departmanlar</span>
                        </div>
                        <Button
                            variant="primary"
                            onClick={(e) => {
                                e.stopPropagation();
                                console.log('🆕 Ana modalı kapat, departman modalını aç');
                                console.log('🏢 selectedCompany:', selectedCompany);
                                
                                // Önce ana modalı kapat
                                setShowDepartmentsModal(false);
                                
                                // Sonra departman oluşturma modalını aç
                                setTimeout(() => {
                                    setShowCreateDepartmentModal(true);
                                    console.log('✅ Departman modalı açıldı');
                                }, 100);
                            }}
                            leftIcon={<Plus className="w-4 h-4" />}
                        >
                            Yeni Departman
                        </Button>
                    </div>

                    {/* Departments List */}
                    <div className="border rounded-lg">
                        {departmentsLoading ? (
                            <div className="flex items-center justify-center py-12">
                                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
                                <span className="ml-2 text-gray-500">Yükleniyor...</span>
                            </div>
                        ) : departments.length === 0 ? (
                            <div className="text-center py-12">
                                <Users className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                                <p className="text-gray-500">Bu şirkette henüz departman bulunmuyor</p>
                                <Button
                                    variant="primary"
                                    onClick={() => {
                                        console.log('🏆 Ana modalı kapat, ilk departman modalını aç');
                                        console.log('🏢 selectedCompany:', selectedCompany);
                                        
                                        // Önce ana modalı kapat
                                        setShowDepartmentsModal(false);
                                        
                                        // Sonra departman oluşturma modalını aç
                                        setTimeout(() => {
                                            setShowCreateDepartmentModal(true);
                                            console.log('✅ İlk departman modalı açıldı');
                                        }, 100);
                                    }}
                                    leftIcon={<Plus className="w-4 h-4" />}
                                    className="mt-4"
                                >
                                    İlk Departmanı Ekle
                                </Button>
                            </div>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="min-w-full divide-y divide-gray-200">
                                    <thead className="bg-gray-50">
                                        <tr>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Departman
                                            </th>
                                            <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Kullanıcı Sayısı
                                            </th>
                                            <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                Ticket Sayısı  
                                            </th>
                                            <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                İşlemler
                                            </th>
                                        </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                        {departments.map((department) => (
                                            <tr key={department.id} className="hover:bg-gray-50">
                                                <td className="px-6 py-4">
                                                    <div>
                                                        <div className="text-sm font-medium text-gray-900">
                                                            {department.name}
                                                        </div>
                                                        {department.description && (
                                                            <div className="text-sm text-gray-500">
                                                                {department.description}
                                                            </div>
                                                        )}
                                                    </div>
                                                </td>
                                                <td className="px-6 py-4 text-center text-sm text-gray-900">
                                                    {department.userCount || 0}
                                                </td>
                                                <td className="px-6 py-4 text-center text-sm text-gray-900">
                                                    {department.ticketCount || 0}
                                                </td>
                                                <td className="px-6 py-4 text-center">
                                                    <div className="flex items-center justify-center gap-2">
                                                        <button
                                                            onClick={() => openEditDepartmentModal(department)}
                                                            className="text-primary-600 hover:text-primary-900"
                                                        >
                                                            <Edit className="w-4 h-4" />
                                                        </button>
                                                        <button
                                                            onClick={() => openDeleteDepartmentModal(department)}
                                                            className="text-danger-600 hover:text-danger-900"
                                                        >
                                                            <Trash2 className="w-4 h-4" />
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>
            </Modal>

            {/* Create Department Modal */}
            <SimpleModal
                isOpen={showCreateDepartmentModal}
                onClose={() => {setShowCreateDepartmentModal(false); resetDepartmentForm();}}
                title="Yeni Departman Ekle"
                size="lg"
            >
                <form 
                    onSubmit={(e) => {
                        e.preventDefault();
                        handleCreateDepartment();
                    }}
                    className="space-y-4"
                >
                    <div>
                        <label htmlFor="dept-name" className="block text-sm font-medium text-gray-700 mb-1">
                            Departman Adı <span className="text-danger-500 ml-1">*</span>
                        </label>
                        <input
                            id="dept-name"
                            name="name"
                            type="text"
                            className="block w-full rounded-md border border-gray-300 bg-white text-gray-900 placeholder-gray-400 px-3 py-2 shadow-sm transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:border-primary-500 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
                            style={{ pointerEvents: 'auto', position: 'relative', zIndex: 100002 }}
                            value={departmentFormData.name}
                            onChange={(e) => {
                                console.log('🖊️ Name input onChange:', e.target.value);
                                setDepartmentFormData(prev => ({ ...prev, name: e.target.value }));
                            }}
                            onFocus={() => console.log('🎯 Name input focused')}
                            onBlur={() => console.log('👻 Name input blurred')}
                            onClick={(e) => {
                                e.stopPropagation();
                                console.log('🖱️ Name input clicked');
                            }}
                            onKeyDown={(e) => console.log('⌨️ Name input key:', e.key)}
                            onMouseDown={() => console.log('🖱️ Name input mousedown')}
                            placeholder="Departman adını giriniz"
                            required
                            autoFocus
                        />
                    </div>
                    <div>
                        <label htmlFor="dept-desc" className="block text-sm font-medium text-gray-700 mb-1">Açıklama</label>
                        <textarea
                            id="dept-desc"
                            name="description"
                            className="block w-full rounded-md border border-gray-300 bg-white text-gray-900 placeholder-gray-400 px-3 py-2 shadow-sm transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:border-primary-500 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
                            style={{ pointerEvents: 'auto', position: 'relative', zIndex: 100002 }}
                            rows={3}
                            value={departmentFormData.description}
                            onChange={(e) => {
                                console.log('🖊️ Description textarea onChange:', e.target.value);
                                setDepartmentFormData(prev => ({ ...prev, description: e.target.value }));
                            }}
                            onFocus={() => console.log('🎯 Description textarea focused')}
                            onBlur={() => console.log('👻 Description textarea blurred')}
                            onClick={(e) => {
                                e.stopPropagation();
                                console.log('🖱️ Description textarea clicked');
                            }}
                            onKeyDown={(e) => console.log('⌨️ Description textarea key:', e.key)}
                            onMouseDown={() => console.log('🖱️ Description textarea mousedown')}
                            placeholder="Departman açıklamasını giriniz (opsiyonel)"
                        />
                    </div>
                    <div className="flex justify-end gap-3 mt-6">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => {setShowCreateDepartmentModal(false); resetDepartmentForm();}}
                        >
                            İptal
                        </Button>
                        <Button
                            type="submit"
                            variant="primary"
                            disabled={!departmentFormData.name.trim()}
                        >
                            Oluştur
                        </Button>
                    </div>
                </form>
            </SimpleModal>

            {/* First Department Modal */}
            <SimpleModal
                isOpen={showFirstDepartmentModal}
                onClose={() => {setShowFirstDepartmentModal(false); resetDepartmentForm();}}
                title="İlk Departmanı Ekle"
                size="lg"
            >
                <form 
                    onSubmit={(e) => {
                        e.preventDefault();
                        console.log('🏆 FIRST DEPARTMENT FORM SUBMIT!');
                        handleCreateDepartment();
                    }}
                    className="space-y-4"
                >
                    <div>
                        <label htmlFor="first-dept-name" className="block text-sm font-medium text-gray-700 mb-1">
                            Departman Adı <span className="text-danger-500 ml-1">*</span>
                        </label>
                        <input
                            id="first-dept-name"
                            name="name"
                            type="text"
                            className="block w-full rounded-md border border-gray-300 bg-white text-gray-900 placeholder-gray-400 px-3 py-2 shadow-sm transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:border-primary-500 focus:ring-primary-500"
                            value={departmentFormData.name}
                            onChange={(e) => {
                                console.log('🏆 FIRST DEPT Name input onChange:', e.target.value);
                                setDepartmentFormData(prev => ({ ...prev, name: e.target.value }));
                            }}
                            onFocus={() => console.log('🎯 FIRST DEPT Name input focused')}
                            onBlur={() => console.log('👻 FIRST DEPT Name input blurred')}
                            onClick={(e) => {
                                e.stopPropagation();
                                console.log('🖱️ FIRST DEPT Name input clicked');
                            }}
                            placeholder="Departman adını giriniz"
                            required
                            autoFocus
                        />
                    </div>
                    <div>
                        <label htmlFor="first-dept-desc" className="block text-sm font-medium text-gray-700 mb-1">Açıklama</label>
                        <textarea
                            id="first-dept-desc"
                            name="description"
                            className="block w-full rounded-md border border-gray-300 bg-white text-gray-900 placeholder-gray-400 px-3 py-2 shadow-sm transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:border-primary-500 focus:ring-primary-500"
                            rows={3}
                            value={departmentFormData.description}
                            onChange={(e) => {
                                console.log('🏆 FIRST DEPT Description onChange:', e.target.value);
                                setDepartmentFormData(prev => ({ ...prev, description: e.target.value }));
                            }}
                            onFocus={() => console.log('🎯 FIRST DEPT Description focused')}
                            onClick={(e) => {
                                e.stopPropagation();
                                console.log('🖱️ FIRST DEPT Description clicked');
                            }}
                            placeholder="Departman açıklamasını giriniz (opsiyonel)"
                        />
                    </div>
                    <div className="flex justify-end gap-3 mt-6">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => {setShowFirstDepartmentModal(false); resetDepartmentForm();}}
                        >
                            İptal
                        </Button>
                        <Button
                            type="submit"
                            variant="primary"
                            disabled={!departmentFormData.name.trim()}
                        >
                            Oluştur
                        </Button>
                    </div>
                </form>
            </SimpleModal>

            {/* Edit Department Modal */}
            <Modal
                isOpen={showEditDepartmentModal}
                onClose={() => {setShowEditDepartmentModal(false); setSelectedDepartment(null); resetDepartmentForm();}}
                title="Departman Düzenle"
                size="lg"
                closeOnOverlayClick={false}
            >
                <div className="space-y-4">
                    <Input
                        label="Departman Adı"
                        value={departmentFormData.name}
                        onChange={(e) => setDepartmentFormData({ ...departmentFormData, name: e.target.value })}
                        required
                    />
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Açıklama</label>
                        <textarea
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                            rows={3}
                            value={departmentFormData.description}
                            onChange={(e) => setDepartmentFormData({ ...departmentFormData, description: e.target.value })}
                        />
                    </div>
                </div>
                <div className="flex justify-end gap-3 mt-6">
                    <Button
                        variant="outline"
                        onClick={() => {setShowEditDepartmentModal(false); setSelectedDepartment(null); resetDepartmentForm();}}
                    >
                        İptal
                    </Button>
                    <Button
                        variant="primary"
                        onClick={handleUpdateDepartment}
                        disabled={!departmentFormData.name.trim()}
                    >
                        Güncelle
                    </Button>
                </div>
            </Modal>

            {/* Delete Department Modal */}
            <Modal
                isOpen={showDeleteDepartmentModal}
                onClose={() => {setShowDeleteDepartmentModal(false); setSelectedDepartment(null);}}
                title="Departman Sil"
                size="md"
                closeOnOverlayClick={false}
            >
                <div className="text-center">
                    <div className="text-danger-600 mb-4">
                        <Users className="w-12 h-12 mx-auto" />
                    </div>
                    <p className="text-gray-600 mb-6">
                        <strong>{selectedDepartment?.name}</strong> departmanını silmek istediğinizden emin misiniz?
                        <br />
                        Bu işlem geri alınamaz ve departmandaki tüm kullanıcılar departmansız kalacak.
                    </p>
                    <div className="flex justify-center gap-3">
                        <Button
                            variant="outline"
                            onClick={() => {setShowDeleteDepartmentModal(false); setSelectedDepartment(null);}}
                        >
                            İptal
                        </Button>
                        <Button
                            variant="danger"
                            onClick={handleDeleteDepartment}
                        >
                            Sil
                        </Button>
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default Companies;