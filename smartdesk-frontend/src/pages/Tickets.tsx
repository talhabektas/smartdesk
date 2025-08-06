import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Select } from '../components/ui/Select';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { Plus, Search, Filter, Eye, Edit, MessageCircle, CheckCircle, XCircle, Clock4 } from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { Ticket, TicketStatus, TicketPriority, TicketCategory, TicketSource } from '../types';
import { ticketService } from '../services/ticketService';
import { formatDate } from '../utils/dateUtils';
import ChatWindow from '../components/chat/ChatWindow';

const Tickets: React.FC = () => {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<TicketStatus | ''>('');
  const [priorityFilter, setPriorityFilter] = useState<TicketPriority | ''>('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedTicketForChat, setSelectedTicketForChat] = useState<Ticket | null>(null);
  const [showChatWindow, setShowChatWindow] = useState(false);
  const [selectedTicketForEdit, setSelectedTicketForEdit] = useState<Ticket | null>(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [approvalLoading, setApprovalLoading] = useState<number | null>(null);

  useEffect(() => {
    loadTickets();
  }, []);

  const loadTickets = async () => {
    try {
      setLoading(true);
      
      // Get company ID from user or JWT token as fallback
      let companyId = user?.company?.id;
      if (!companyId) {
        const token = localStorage.getItem('accessToken');
        if (token) {
          try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            companyId = payload.companyId;
          } catch (e) {
            console.warn('Failed to decode JWT token for company ID');
          }
        }
      }
      
      const response = await ticketService.getTickets({
        companyId: companyId || 1,
        userRole: user?.role,
        userId: user?.id
      });
      console.log('🎫 Ticket loading response:', response);
      console.log('🎫 Response data:', response.data);
      console.log('🎫 Setting tickets:', response.data || []);
      console.log('🎫 Data type:', typeof response.data, Array.isArray(response.data));
      
      // Debug: Log all ticket statuses
      if (Array.isArray(response.data)) {
        const statusCounts = response.data.reduce((acc: any, ticket: any) => {
          acc[ticket.status] = (acc[ticket.status] || 0) + 1;
          return acc;
        }, {});
        console.log('🎫 Ticket status counts:', statusCounts);
      }
      
      setTickets(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Failed to load tickets:', error);
    } finally {
      setLoading(false);
    }
  };

  const filteredTickets = (tickets || []).filter(ticket => {
    const matchesSearch = ticket.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      ticket.ticketNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (ticket.customer?.companyName || '').toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus = !statusFilter || ticket.status === statusFilter;
    const matchesPriority = !priorityFilter || ticket.priority === priorityFilter;

    // Debug logging for RESOLVED tickets
    if (ticket.status === 'RESOLVED') {
      console.log('🔍 Found RESOLVED ticket:', ticket.ticketNumber, 'Filter:', statusFilter, 'Match:', matchesStatus);
    }

    return matchesSearch && matchesStatus && matchesPriority;
  });

  const getStatusColor = (status: TicketStatus) => {
    switch (status) {
      case TicketStatus.NEW: return 'bg-blue-100 text-blue-800';
      case TicketStatus.OPEN: return 'bg-green-100 text-green-800';
      case TicketStatus.IN_PROGRESS: return 'bg-yellow-100 text-yellow-800';
      case TicketStatus.RESOLVED: return 'bg-green-100 text-green-800';
      case TicketStatus.CLOSED: return 'bg-gray-100 text-gray-800';
      case TicketStatus.PENDING_MANAGER_APPROVAL: return 'bg-orange-100 text-orange-800';
      case TicketStatus.MANAGER_APPROVED: return 'bg-indigo-100 text-indigo-800';
      case TicketStatus.PENDING_ADMIN_APPROVAL: return 'bg-pink-100 text-pink-800';
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

  const handleChatClick = (ticket: Ticket) => {
    setSelectedTicketForChat(ticket);
    setShowChatWindow(true);
  };

  const handleCloseChatWindow = () => {
    setShowChatWindow(false);
    setSelectedTicketForChat(null);
  };

  // Role-based ticket editing permission
  const canEditTicket = (ticket: Ticket): boolean => {
    if (!user) return false;

    const userRole = user.role;
    
    // SUPER_ADMIN can edit all tickets
    if (userRole === 'SUPER_ADMIN') return true;
    
    // MANAGER can edit tickets in their company
    if (userRole === 'MANAGER' && user.company?.id === ticket.companyId) return true;
    
    // AGENT can edit tickets assigned to them or unassigned tickets in their company
    if (userRole === 'AGENT') {
      const sameCompany = user.company?.id === ticket.companyId;
      const assignedToUser = ticket.assignedAgent?.id === user.id;
      const unassigned = !ticket.assignedAgent;
      return sameCompany && (assignedToUser || unassigned);
    }
    
    // CUSTOMER can edit their own tickets
    if (userRole === 'CUSTOMER') {
      return ticket.customer?.id === user.id;
    }
    
    return false;
  };

  const handleEditTicket = (ticket: Ticket) => {
    console.log('Edit ticket:', ticket.id, 'User role:', user?.role);
    setSelectedTicketForEdit(ticket);
    setShowEditModal(true);
  };

  // Approval workflow handlers
  const handleResolveTicket = async (ticketId: number) => {
    try {
      setApprovalLoading(ticketId);
      await ticketService.resolveTicket(ticketId, 'Ticket resolved - awaiting manager approval');
      await loadTickets(); // Refresh the ticket list
    } catch (error) {
      console.error('Failed to resolve ticket:', error);
    } finally {
      setApprovalLoading(null);
    }
  };

  const handleManagerApproval = async (ticketId: number) => {
    try {
      setApprovalLoading(ticketId);
      await ticketService.approveByManager(ticketId, 'Approved by manager');
      await loadTickets(); // Refresh the ticket list
    } catch (error) {
      console.error('Failed to approve ticket as manager:', error);
    } finally {
      setApprovalLoading(null);
    }
  };

  const handleAdminApproval = async (ticketId: number) => {
    try {
      setApprovalLoading(ticketId);
      await ticketService.approveByAdmin(ticketId, 'Final approval by admin');
      await loadTickets(); // Refresh the ticket list
    } catch (error) {
      console.error('Failed to approve ticket as admin:', error);
    } finally {
      setApprovalLoading(null);
    }
  };

  const handleRejectApproval = async (ticketId: number) => {
    try {
      setApprovalLoading(ticketId);
      await ticketService.rejectApproval(ticketId, 'Approval rejected - requires revision');
      await loadTickets(); // Refresh the ticket list
    } catch (error) {
      console.error('Failed to reject approval:', error);
    } finally {
      setApprovalLoading(null);
    }
  };

  // Render approval buttons based on ticket status and user role
  const renderApprovalButtons = (ticket: Ticket) => {
    if (!user) return null;

    const userRole = user.role;
    const ticketStatus = ticket.status as string;
    const isLoading = approvalLoading === ticket.id;


    // IN_PROGRESS tickets - can be resolved for approval by MANAGER or AGENT
    if (ticketStatus === 'IN_PROGRESS' && (userRole === 'MANAGER' || userRole === 'AGENT')) {
      return (
        <Button 
          variant="ghost" 
          size="sm"
          onClick={() => handleResolveTicket(ticket.id)}
          disabled={isLoading}
          title="Send for Manager Approval"
        >
          {isLoading ? <Clock4 className="w-4 h-4 animate-spin" /> : <CheckCircle className="w-4 h-4 text-blue-600" />}
        </Button>
      );
    }

    // PENDING_MANAGER_APPROVAL - can be approved by MANAGER
    if (ticketStatus === 'PENDING_MGR' && userRole === 'MANAGER') {
      return (
        <>
          <Button 
            variant="ghost" 
            size="sm"
            onClick={() => handleManagerApproval(ticket.id)}
            disabled={isLoading}
            title="Approve as Manager"
          >
            {isLoading ? <Clock4 className="w-4 h-4 animate-spin" /> : <CheckCircle className="w-4 h-4 text-green-600" />}
          </Button>
          <Button 
            variant="ghost" 
            size="sm"
            onClick={() => handleRejectApproval(ticket.id)}
            disabled={isLoading}
            title="Reject Approval"
          >
            <XCircle className="w-4 h-4 text-red-600" />
          </Button>
        </>
      );
    }

    // PENDING_ADMIN_APPROVAL - can be approved by SUPER_ADMIN
    if (ticketStatus === 'PENDING_ADMIN' && userRole === 'SUPER_ADMIN') {
      return (
        <>
          <Button 
            variant="ghost" 
            size="sm"
            onClick={() => handleAdminApproval(ticket.id)}
            disabled={isLoading}
            title="Final Approval (Resolve Ticket)"
          >
            {isLoading ? <Clock4 className="w-4 h-4 animate-spin" /> : <CheckCircle className="w-4 h-4 text-green-600" />}
          </Button>
          <Button 
            variant="ghost" 
            size="sm"
            onClick={() => handleRejectApproval(ticket.id)}
            disabled={isLoading}
            title="Reject Final Approval"
          >
            <XCircle className="w-4 h-4 text-red-600" />
          </Button>
        </>
      );
    }

    return null;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  // Debug modal state
  console.log('Tickets component render - showCreateModal:', showCreateModal);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-title-1 text-gray-900 font-bold">
            Tickets
          </h1>
          <p className="text-body text-gray-600 mt-1">
            Manage and track all support tickets
          </p>
        </div>

        <Button
          variant="primary"
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            console.log('New Ticket button clicked, setting showCreateModal to true');
            setShowCreateModal(true);
          }}
        >
          New Ticket
        </Button>
      </div>

      {/* Filters */}
      <Card className="p-4">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
            <Input
              placeholder="Search tickets..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>

          <Select
            value={statusFilter}
            onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setStatusFilter(e.target.value as TicketStatus | '')}
          >
            <option value="">All Status</option>
            {Object.values(TicketStatus).map(status => (
              <option key={status} value={status}>{status.replace('_', ' ')}</option>
            ))}
          </Select>

          <Select
            value={priorityFilter}
            onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setPriorityFilter(e.target.value as TicketPriority | '')}
          >
            <option value="">All Priority</option>
            {Object.values(TicketPriority).map(priority => (
              <option key={priority} value={priority}>{priority}</option>
            ))}
          </Select>

          <Button variant="outline" onClick={() => {
            setSearchTerm('');
            setStatusFilter('');
            setPriorityFilter('');
          }}>
            Clear Filters
          </Button>
        </div>
      </Card>

      {/* Tickets List */}
      <div className="space-y-4">
        {filteredTickets.length === 0 ? (
          <Card className="p-16 text-center">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <Search className="w-8 h-8 text-gray-400" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              {tickets.length === 0 ? 'No tickets found' : 'No tickets match your filters'}
            </h3>
            <p className="text-gray-600 mb-4">
              {tickets.length === 0
                ? 'Create your first ticket to get started'
                : 'Try adjusting your search criteria'
              }
            </p>
            {tickets.length === 0 && (
              <Button variant="primary" onClick={() => setShowCreateModal(true)}>
                Create First Ticket
              </Button>
            )}
          </Card>
        ) : (
          filteredTickets.map(ticket => (
            <Card key={ticket.id} className="p-6 hover:shadow-md transition-shadow">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <h3 className="text-lg font-semibold text-gray-900">
                      #{ticket.ticketNumber}
                    </h3>
                    <Badge className={getStatusColor(ticket.status)}>
                      {ticket.status.replace('_', ' ')}
                    </Badge>
                    <Badge className={getPriorityColor(ticket.priority)}>
                      {ticket.priority}
                    </Badge>
                  </div>

                  <h4 className="text-base font-medium text-gray-900 mb-2">
                    {ticket.title}
                  </h4>

                  <p className="text-sm text-gray-600 mb-3 line-clamp-2">
                    {ticket.description}
                  </p>

                  <div className="flex items-center gap-4 text-sm text-gray-500">
                    <span>Customer: {ticket.customerName || ticket.customerCompanyName || ticket.customer?.companyName || 'Unknown'}</span>
                    <span>Created: {formatDate(ticket.createdAt)}</span>
                    {ticket.assignedAgent && (
                      <span>Assigned to: {ticket.assignedAgent.firstName} {ticket.assignedAgent.lastName}</span>
                    )}
                  </div>
                </div>

                <div className="flex items-center gap-2 ml-4">
                  <Button 
                    variant="ghost" 
                    size="sm"
                    onClick={() => navigate(`/tickets/${ticket.id}`)}  
                  >
                    <Eye className="w-4 h-4" />
                  </Button>
                  {canEditTicket(ticket) && (
                    <Button 
                      variant="ghost" 
                      size="sm"
                      onClick={() => handleEditTicket(ticket)}
                    >
                      <Edit className="w-4 h-4" />
                    </Button>
                  )}
                  
                  {/* Approval Workflow Buttons */}
                  {renderApprovalButtons(ticket)}
                  
                  <Button 
                    variant="ghost" 
                    size="sm"
                    onClick={() => handleChatClick(ticket)}
                  >
                    <MessageCircle className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            </Card>
          ))
        )}
      </div>

      {/* Debug: Show modal state */}
      <div className="fixed top-4 right-4 bg-red-100 p-2 rounded text-sm">
        Modal State: {showCreateModal ? 'OPEN' : 'CLOSED'}
      </div>

      {/* Create Ticket Modal */}
      <CreateTicketModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSuccess={() => {
          setShowCreateModal(false);
          loadTickets();
        }}
      />

      {/* Edit Ticket Modal */}
      <EditTicketModal
        ticket={selectedTicketForEdit}
        isOpen={showEditModal}
        onClose={() => {
          setShowEditModal(false);
          setSelectedTicketForEdit(null);
        }}
        onSuccess={() => {
          setShowEditModal(false);
          setSelectedTicketForEdit(null);
          loadTickets();
        }}
      />

      {/* Chat Window */}
      {showChatWindow && selectedTicketForChat && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="w-full max-w-4xl h-[80vh] bg-white rounded-lg shadow-xl">
            <ChatWindow
              ticketId={selectedTicketForChat.id}
              ticketNumber={selectedTicketForChat.ticketNumber}
              ticketTitle={selectedTicketForChat.title}
              onClose={handleCloseChatWindow}
              isOpen={showChatWindow}
            />
          </div>
        </div>
      )}
    </div>
  );
};

// Create Ticket Modal Component
interface CreateTicketModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const CreateTicketModal: React.FC<CreateTicketModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const { user } = useAuthStore();
  
  // Debug logging
  console.log('CreateTicketModal render - isOpen:', isOpen);
  
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    priority: TicketPriority.NORMAL,
    category: TicketCategory.TECHNICAL_SUPPORT,
    source: TicketSource.WEB_FORM,
    customerId: undefined as number | undefined,
    departmentId: undefined as number | undefined
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      // Debug user information
      console.log('🔍 Current user for ticket creation:', user);
      console.log('🔍 User company:', user?.company);
      console.log('🔍 User company ID:', user?.company?.id);
      console.log('🔍 User role:', user?.role);
      console.log('🔍 User ID:', user?.id);
      
      // Get company ID from JWT token if user.company is missing
      let companyId = user?.company?.id;
      if (!companyId) {
        // Try to get from JWT token as fallback
        const token = localStorage.getItem('accessToken');
        if (token) {
          try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            companyId = payload.companyId;
            console.log('🔍 Extracted company ID from JWT:', companyId);
          } catch (e) {
            console.warn('Failed to decode JWT token for company ID');
          }
        }
      }
      
      // Add companyId and customerId - CUSTOMER should use their own info
      const ticketData = {
        ...formData,
        companyId: companyId || 1, // Default to 1 as last resort
        customerId: user?.role === 'CUSTOMER' ? user.id : formData.customerId // Customer creates ticket for themselves
      };

      console.log('🎫 Frontend sending ticket data:', ticketData);
      await ticketService.createTicket(ticketData);
      onSuccess();
    } catch (error: any) {
      console.error('Failed to create ticket:', error);
      console.error('Error response:', error.response?.data);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Create New Ticket"
      size="2xl"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Title *
          </label>
          <Input
            value={formData.title}
            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            placeholder="Enter ticket title"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Description *
          </label>
          <textarea
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            placeholder="Describe the issue..."
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
            rows={4}
            required
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Priority
            </label>
            <Select
              value={formData.priority}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setFormData({ ...formData, priority: e.target.value as TicketPriority })}
            >
              {Object.values(TicketPriority).map(priority => (
                <option key={priority} value={priority}>{priority}</option>
              ))}
            </Select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Category
            </label>
            <Select
              value={formData.category}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setFormData({ ...formData, category: e.target.value as TicketCategory })}
            >
              {Object.values(TicketCategory).map(category => (
                <option key={category} value={category}>{category.replace('_', ' ')}</option>
              ))}
            </Select>
          </div>
        </div>

        <div className="flex justify-end gap-3 pt-4">
          <Button
            type="button"
            variant="secondary"
            onClick={onClose}
            disabled={loading}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            disabled={loading}
          >
            {loading ? 'Creating...' : 'Create Ticket'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

// Edit Ticket Modal Component
interface EditTicketModalProps {
  ticket: Ticket | null;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const EditTicketModal: React.FC<EditTicketModalProps> = ({ ticket, isOpen, onClose, onSuccess }) => {
  const { user } = useAuthStore();
  
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    priority: TicketPriority.NORMAL,
    status: TicketStatus.NEW,
    category: TicketCategory.TECHNICAL_SUPPORT,
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (ticket) {
      setFormData({
        title: ticket.title || '',
        description: ticket.description || '',
        priority: ticket.priority || TicketPriority.NORMAL,
        status: ticket.status || TicketStatus.NEW,
        category: ticket.category || TicketCategory.TECHNICAL_SUPPORT,
      });
    }
  }, [ticket]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ticket) return;
    
    setLoading(true);

    try {
      console.log('🎫 Updating ticket:', ticket.id, formData);
      await ticketService.updateTicket(ticket.id, formData);
      onSuccess();
    } catch (error: any) {
      console.error('Failed to update ticket:', error);
      console.error('Error response:', error.response?.data);
    } finally {
      setLoading(false);
    }
  };

  if (!ticket) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Edit Ticket #${ticket.ticketNumber}`}
      size="2xl"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Title *
          </label>
          <Input
            value={formData.title}
            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            placeholder="Enter ticket title"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Description *
          </label>
          <textarea
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            placeholder="Describe the issue..."
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
            rows={4}
            required
          />
        </div>

        <div className="grid grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Priority
            </label>
            <Select
              value={formData.priority}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setFormData({ ...formData, priority: e.target.value as TicketPriority })}
            >
              {Object.values(TicketPriority).map(priority => (
                <option key={priority} value={priority}>{priority}</option>
              ))}
            </Select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Status
            </label>
            <Select
              value={formData.status}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setFormData({ ...formData, status: e.target.value as TicketStatus })}
            >
              {Object.values(TicketStatus).map(status => (
                <option key={status} value={status}>{status.replace('_', ' ')}</option>
              ))}
            </Select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Category
            </label>
            <Select
              value={formData.category}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setFormData({ ...formData, category: e.target.value as TicketCategory })}
            >
              {Object.values(TicketCategory).map(category => (
                <option key={category} value={category}>{category.replace('_', ' ')}</option>
              ))}
            </Select>
          </div>
        </div>

        <div className="flex justify-end gap-3 pt-4">
          <Button
            type="button"
            variant="secondary"
            onClick={onClose}
            disabled={loading}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            disabled={loading}
          >
            {loading ? 'Updating...' : 'Update Ticket'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default Tickets;