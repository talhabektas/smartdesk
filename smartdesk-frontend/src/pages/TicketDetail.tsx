import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import ChatWindow from '../components/chat/ChatWindow';
import { 
  ArrowLeft, 
  Edit, 
  MessageCircle, 
  User, 
  Calendar, 
  Clock, 
  Tag,
  Building,
  Mail,
  Phone,
  AlertCircle,
  CheckCircle,
  FileText,
  Download,
  Paperclip
} from 'lucide-react';
import { Ticket, TicketStatus, TicketPriority, UserRole } from '../types';
import { ticketService } from '../services/ticketService';
import { formatDate } from '../utils/dateUtils';
import { toast } from 'react-hot-toast';

const TicketDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  
  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [loading, setLoading] = useState(true);
  const [showChatWindow, setShowChatWindow] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);

  useEffect(() => {
    if (id) {
      loadTicketDetail();
    }
  }, [id]);

  const loadTicketDetail = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      const response = await ticketService.getTicketById(parseInt(id));
      setTicket(response);
    } catch (error) {
      console.error('Failed to load ticket:', error);
      toast.error('Ticket yüklenirken hata oluştu');
      navigate('/tickets');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (newStatus: TicketStatus) => {
    if (!ticket) return;

    try {
      await ticketService.updateTicketStatus(ticket.id, newStatus);
      setTicket(prev => prev ? { ...prev, status: newStatus } : null);
      toast.success('Ticket durumu güncellendi');
    } catch (error) {
      console.error('Failed to update ticket status:', error);
      toast.error('Durum güncellenirken hata oluştu');
    }
  };

  const getStatusColor = (status: TicketStatus) => {
    switch (status) {
      case TicketStatus.NEW: return 'bg-blue-100 text-blue-800';
      case TicketStatus.OPEN: return 'bg-green-100 text-green-800';
      case TicketStatus.IN_PROGRESS: return 'bg-yellow-100 text-yellow-800';
      case TicketStatus.PENDING: return 'bg-orange-100 text-orange-800';
      case TicketStatus.RESOLVED: return 'bg-purple-100 text-purple-800';
      case TicketStatus.CLOSED: return 'bg-gray-100 text-gray-800';
      case TicketStatus.CANCELLED: return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getPriorityColor = (priority: TicketPriority) => {
    switch (priority) {
      case TicketPriority.LOW: return 'bg-gray-100 text-gray-800';
      case TicketPriority.NORMAL: return 'bg-blue-100 text-blue-800';
      case TicketPriority.HIGH: return 'bg-orange-100 text-orange-800';
      case TicketPriority.URGENT: return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const canEditTicket = () => {
    if (!user || !ticket) return false;
    
    return user.role === UserRole.SUPER_ADMIN || 
           user.role === UserRole.MANAGER || 
           user.role === UserRole.AGENT ||
           (user.role === UserRole.CUSTOMER && ticket.customerId === user.id);
  };

  const canChangeStatus = () => {
    if (!user) return false;
    
    return user.role === UserRole.SUPER_ADMIN || 
           user.role === UserRole.MANAGER || 
           user.role === UserRole.AGENT;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!ticket) {
    return (
      <div className="text-center py-12">
        <h3 className="text-lg font-medium text-gray-900 mb-2">Ticket bulunamadı</h3>
        <Button onClick={() => navigate('/tickets')}>
          Ticket Listesine Dön
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate('/tickets')}
            leftIcon={<ArrowLeft className="w-4 h-4" />}
          >
            Geri
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              #{ticket.ticketNumber}
            </h1>
            <p className="text-gray-600">{ticket.title}</p>
          </div>
        </div>
        
        <div className="flex items-center gap-3">
          {canEditTicket() && (
            <Button
              variant="secondary"
              leftIcon={<Edit className="w-4 h-4" />}
              onClick={() => setShowEditModal(true)}
            >
              Düzenle
            </Button>
          )}
          <Button
            variant="primary"
            leftIcon={<MessageCircle className="w-4 h-4" />}
            onClick={() => setShowChatWindow(true)}
          >
            Chat
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Ticket Details */}
          <Card>
            <div className="flex items-start justify-between mb-6">
              <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-2">
                  Ticket Detayları
                </h2>
                <div className="flex items-center gap-2">
                  <Badge className={getStatusColor(ticket.status)}>
                    {ticket.status}
                  </Badge>
                  <Badge className={getPriorityColor(ticket.priority)}>
                    {ticket.priority}
                  </Badge>
                </div>
              </div>
              
              {canChangeStatus() && (
                <div className="flex items-center gap-2">
                  <select
                    value={ticket.status}
                    onChange={(e) => handleStatusChange(e.target.value as TicketStatus)}
                    className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    {Object.values(TicketStatus).map(status => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                </div>
              )}
            </div>
            
            <div className="prose max-w-none">
              <p className="text-gray-700 whitespace-pre-wrap">
                {ticket.description}
              </p>
            </div>

            {/* Attachments */}
            {ticket.attachments && ticket.attachments.length > 0 && (
              <div className="mt-6 pt-6 border-t border-gray-200">
                <h3 className="text-sm font-medium text-gray-900 mb-3 flex items-center gap-2">
                  <Paperclip className="w-4 h-4" />
                  Ekler ({ticket.attachments.length})
                </h3>
                <div className="space-y-2">
                  {ticket.attachments.map((attachment, index) => (
                    <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                      <div className="flex items-center gap-3">
                        <FileText className="w-5 h-5 text-gray-400" />
                        <div>
                          <p className="text-sm font-medium text-gray-900">
                            {attachment.fileName}
                          </p>
                          <p className="text-xs text-gray-500">
                            {attachment.fileSize} • {attachment.uploadedAt}
                          </p>
                        </div>
                      </div>
                      <Button variant="ghost" size="sm">
                        <Download className="w-4 h-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </Card>

          {/* Activity/Comments Timeline */}
          <Card>
            <h2 className="text-lg font-semibold text-gray-900 mb-6">
              Aktivite Geçmişi
            </h2>
            
            <div className="space-y-4">
              {/* Created Event */}
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                  <AlertCircle className="w-4 h-4 text-blue-600" />
                </div>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-gray-900">
                      {ticket.customer ? `${ticket.customer.firstName} ${ticket.customer.lastName}` : 'Müşteri'}
                    </span>
                    <span className="text-sm text-gray-500">ticket oluşturdu</span>
                  </div>
                  <p className="text-sm text-gray-500">
                    {formatDate(ticket.createdAt)}
                  </p>
                </div>
              </div>

              {/* Status Changes and Comments would go here */}
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                </div>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-gray-900">
                      {ticket.assignedAgent ? `${ticket.assignedAgent.firstName} ${ticket.assignedAgent.lastName}` : 'Agent'}
                    </span>
                    <span className="text-sm text-gray-500">durumu güncelledi</span>
                  </div>
                  <p className="text-sm text-gray-500">
                    {formatDate(ticket.updatedAt)}
                  </p>
                </div>
              </div>
            </div>
          </Card>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Ticket Information */}
          <Card>
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Ticket Bilgileri
            </h3>
            
            <div className="space-y-4">
              <div className="flex items-center gap-3">
                <Tag className="w-4 h-4 text-gray-400" />
                <div>
                  <p className="text-sm text-gray-500">Kategori</p>
                  <p className="font-medium text-gray-900">{ticket.category}</p>
                </div>
              </div>
              
              <div className="flex items-center gap-3">
                <Calendar className="w-4 h-4 text-gray-400" />
                <div>
                  <p className="text-sm text-gray-500">Oluşturulma</p>
                  <p className="font-medium text-gray-900">
                    {formatDate(ticket.createdAt)}
                  </p>
                </div>
              </div>
              
              <div className="flex items-center gap-3">
                <Clock className="w-4 h-4 text-gray-400" />
                <div>
                  <p className="text-sm text-gray-500">Son Güncelleme</p>
                  <p className="font-medium text-gray-900">
                    {formatDate(ticket.updatedAt)}
                  </p>
                </div>
              </div>

              {ticket.slaDeadline && (
                <div className="flex items-center gap-3">
                  <AlertCircle className="w-4 h-4 text-gray-400" />
                  <div>
                    <p className="text-sm text-gray-500">SLA Bitiş</p>
                    <p className="font-medium text-gray-900">
                      {formatDate(ticket.slaDeadline)}
                    </p>
                  </div>
                </div>
              )}
            </div>
          </Card>

          {/* Customer Information */}
          {ticket.customer && (
            <Card>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">
                Müşteri Bilgileri
              </h3>
              
              <div className="space-y-4">
                <div className="flex items-center gap-3">
                  <User className="w-4 h-4 text-gray-400" />
                  <div>
                    <p className="font-medium text-gray-900">
                      {`${ticket.customer.firstName} ${ticket.customer.lastName}`}
                    </p>
                    <p className="text-sm text-gray-500">{ticket.customer.email}</p>
                  </div>
                </div>
                
                {ticket.customer.phone && (
                  <div className="flex items-center gap-3">
                    <Phone className="w-4 h-4 text-gray-400" />
                    <div>
                      <p className="text-sm text-gray-500">Telefon</p>
                      <p className="font-medium text-gray-900">
                        {ticket.customer.phone}
                      </p>
                    </div>
                  </div>
                )}
                
                {ticket.customer.companyName && (
                  <div className="flex items-center gap-3">
                    <Building className="w-4 h-4 text-gray-400" />
                    <div>
                      <p className="text-sm text-gray-500">Şirket</p>
                      <p className="font-medium text-gray-900">
                        {ticket.customer.companyName}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </Card>
          )}

          {/* Assigned Agent */}
          {ticket.assignedAgent && (
            <Card>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">
                Atanmış Agent
              </h3>
              
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                  <User className="w-5 h-5 text-blue-600" />
                </div>
                <div>
                  <p className="font-medium text-gray-900">
                    {`${ticket.assignedAgent.firstName} ${ticket.assignedAgent.lastName}`}
                  </p>
                  <p className="text-sm text-gray-500">
                    {ticket.assignedAgent.email}
                  </p>
                </div>
              </div>
            </Card>
          )}
        </div>
      </div>

      {/* Chat Window Modal */}
      {showChatWindow && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="w-full max-w-4xl h-[80vh] bg-white rounded-lg shadow-xl">
            <ChatWindow
              ticketId={ticket.id}
              ticketNumber={ticket.ticketNumber}
              ticketTitle={ticket.title}
              onClose={() => setShowChatWindow(false)}
              isOpen={showChatWindow}
            />
          </div>
        </div>
      )}

      {/* Edit Modal */}
      {showEditModal && (
        <Modal
          isOpen={showEditModal}
          onClose={() => setShowEditModal(false)}
          title="Ticket Düzenle"
          size="2xl"
        >
          <div className="space-y-4">
            <p className="text-gray-600">
              Ticket düzenleme özelliği yakında eklenecek.
            </p>
            <div className="flex justify-end">
              <Button
                variant="secondary"
                onClick={() => setShowEditModal(false)}
              >
                Kapat
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default TicketDetail;