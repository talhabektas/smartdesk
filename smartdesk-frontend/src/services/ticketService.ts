import { apiClient } from './apiClient';
import { Ticket, CreateTicketRequest, UpdateTicketRequest } from '../types';

export const ticketService = {
    // Get all tickets - SUPER_ADMIN gets all, CUSTOMER gets own tickets, others get company tickets
    async getTickets(params?: {
        page?: number;
        size?: number;
        status?: string;
        priority?: string;
        search?: string;
        companyId?: number;
        userRole?: string;
        userId?: number;
    }) {
        // Use search endpoint with companyId
        if (params?.search || params?.status || params?.priority) {
            const searchParams: any = {
                companyId: params.companyId || 1,
                page: params.page || 0,
                size: params.size || 20
            };
            if (params.search) searchParams.q = params.search;
            if (params.status) searchParams.status = params.status;
            if (params.priority) searchParams.priority = params.priority;
            
            const response = await apiClient.get('/tickets/search', { params: searchParams });
            console.log('🎫 TicketService search response:', response);
            return {
                data: response.data?.tickets || [],
                totalElements: response.data?.totalElements || 0,
                totalPages: response.data?.totalPages || 0
            };
        } else {
            // SUPER_ADMIN gets all tickets
            if (params?.userRole === 'SUPER_ADMIN') {
                console.log('🎫 SUPER_ADMIN detected - fetching ALL tickets');
                const response = await apiClient.get('/tickets/all', { 
                    params: { 
                        page: params?.page || 0, 
                        size: params?.size || 20 
                    } 
                });
                console.log('🎫 TicketService ALL tickets response:', response);
                return {
                    data: response.data?.tickets || [],
                    totalElements: response.data?.totalElements || 0,
                    totalPages: response.data?.totalPages || 0
                };
            } 
            // CUSTOMER gets only their own tickets
            else if (params?.userRole === 'CUSTOMER') {
                console.log('🎫 CUSTOMER detected - fetching my tickets');
                const response = await apiClient.get('/tickets/my', { 
                    params: { 
                        page: params?.page || 0, 
                        size: params?.size || 20,
                        sort: 'createdAt,desc'  // Yeni ticket'lar en üstte
                    } 
                });
                console.log('🎫 TicketService my tickets response:', response);
                return {
                    data: response.data?.tickets || [],
                    totalElements: response.data?.totalElements || 0,
                    totalPages: response.data?.totalPages || 0
                };
            }
            // Others (MANAGER, AGENT) get company tickets
            else {
                console.log('🎫 MANAGER/AGENT detected - fetching company tickets for companyId:', params?.companyId);
                const response = await apiClient.get(`/tickets/company/${params?.companyId || 1}`, { 
                    params: { 
                        page: params?.page || 0, 
                        size: params?.size || 20,
                        sort: 'createdAt,desc'  // Yeni ticket'lar en üstte
                    } 
                });
                console.log('🎫 TicketService company tickets response:', response);
                // Backend returns custom response with "tickets" array (not "content")
                return {
                    data: response.data?.tickets || [],
                    totalElements: response.data?.totalElements || 0,
                    totalPages: response.data?.totalPages || 0
                };
            }
        }
    },

    // Get ticket by ID
    async getTicketById(id: number) {
        const response = await apiClient.get(`/tickets/${id}`);
        return response.data;
    },

    // Create new ticket
    async createTicket(ticketData: CreateTicketRequest) {
        const response = await apiClient.post('/tickets', ticketData);
        return response.data;
    },

    // Update ticket
    async updateTicket(id: number, ticketData: UpdateTicketRequest) {
        const response = await apiClient.put(`/tickets/${id}`, ticketData);
        return response.data;
    },

    // Change ticket status
    async changeTicketStatus(id: number, status: string) {
        const response = await apiClient.patch(`/tickets/${id}/status`, { status });
        return response.data;
    },

    // Update ticket status (alias for compatibility)
    async updateTicketStatus(id: number, status: string) {
        return this.changeTicketStatus(id, status);
    },

    // Change ticket priority
    async changeTicketPriority(id: number, priority: string) {
        const response = await apiClient.patch(`/tickets/${id}/priority`, { priority });
        return response.data;
    },

    // Assign ticket to agent
    async assignTicket(id: number, agentId: number) {
        const response = await apiClient.patch(`/tickets/${id}/assign`, { agentId });
        return response.data;
    },

    // Close ticket
    async closeTicket(id: number, resolution?: string) {
        const response = await apiClient.patch(`/tickets/${id}/close`, { resolution });
        return response.data;
    },

    // Get ticket comments
    async getTicketComments(id: number, includeInternal = false) {
        const response = await apiClient.get(`/tickets/${id}/comments`, {
            params: { includeInternal }
        });
        return response.data;
    },

    // Add comment to ticket
    async addComment(id: number, commentData: {
        content: string;
        isInternal?: boolean;
    }) {
        const response = await apiClient.post(`/tickets/${id}/comments`, commentData);
        return response.data;
    },

    // Add attachment to ticket
    async addAttachment(id: number, file: File) {
        const formData = new FormData();
        formData.append('file', file);

        const response = await apiClient.post(`/tickets/${id}/attachments`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    },

    // Get tickets by customer
    async getTicketsByCustomer(customerId: number, params?: {
        page?: number;
        size?: number;
    }) {
        const response = await apiClient.get(`/tickets/customer/${customerId}`, { params });
        return response.data;
    },

    // Get tickets by agent
    async getTicketsByAgent(agentId: number, params?: {
        page?: number;
        size?: number;
    }) {
        const response = await apiClient.get(`/tickets/agent/${agentId}`, { params });
        return response.data;
    },

    // Search tickets
    async searchTickets(params: {
        companyId: number;
        q?: string;
        status?: string;
        priority?: string;
        departmentId?: number;
        agentId?: number;
        customerId?: number;
        page?: number;
        size?: number;
    }) {
        const response = await apiClient.get('/tickets/search', { params });
        return response.data;
    },

    // Get ticket statistics
    async getTicketStats(companyId: number) {
        const response = await apiClient.get('/tickets/stats', {
            params: { companyId }
        });
        return response.data;
    },

    // Get active tickets
    async getActiveTickets(companyId: number, params?: {
        page?: number;
        size?: number;
    }) {
        const response = await apiClient.get('/tickets/active', {
            params: { companyId, ...params }
        });
        return response.data;
    },

    // Get unassigned tickets
    async getUnassignedTickets(companyId: number) {
        const response = await apiClient.get('/tickets/unassigned', {
            params: { companyId }
        });
        return response.data;
    },

    // Get tickets at SLA risk
    async getTicketsAtSlaRisk(companyId: number) {
        const response = await apiClient.get('/tickets/sla-risk', {
            params: { companyId }
        });
        return response.data;
    },

    // Add satisfaction rating
    async addSatisfactionRating(id: number, rating: number, feedback?: string) {
        const response = await apiClient.post(`/tickets/${id}/satisfaction`, {
            rating,
            feedback
        });
        return response.data;
    },

    // Get ticket creation trends
    async getTicketCreationTrend(companyId: number, days = 30) {
        const response = await apiClient.get('/tickets/trends/creation', {
            params: { companyId, days }
        });
        return response.data;
    },

    // ============ APPROVAL WORKFLOW METHODS ============

    // Resolve ticket for manager approval
    async resolveTicket(id: number, resolutionSummary?: string) {
        const response = await apiClient.patch(`/tickets/${id}/resolve`, {
            resolutionSummary: resolutionSummary || ''
        });
        return response.data;
    },

    // Manager approval
    async approveByManager(id: number, approvalComment?: string) {
        const response = await apiClient.patch(`/tickets/${id}/approve-manager`, {
            approvalComment: approvalComment || ''
        });
        return response.data;
    },

    // Admin final approval
    async approveByAdmin(id: number, finalComment?: string) {
        const response = await apiClient.patch(`/tickets/${id}/approve-admin`, {
            finalComment: finalComment || ''
        });
        return response.data;
    },

    // Reject approval
    async rejectApproval(id: number, rejectionReason: string) {
        const response = await apiClient.patch(`/tickets/${id}/reject-approval`, {
            rejectionReason
        });
        return response.data;
    }
}; 